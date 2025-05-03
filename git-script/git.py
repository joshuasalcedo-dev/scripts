"""
GitMetadata - A Python library for extracting Git metadata in JSON-friendly formats

This library provides classes to retrieve and structure various Git information:
- Logs
- Commits
- Files (tracked/untracked)
- Branches
- Submodules
- Tags
- Status
"""

import os
import json
import subprocess
import datetime
from typing import List, Dict, Any, Optional, Union, Tuple
from dataclasses import dataclass, field, asdict


class GitCommandError(Exception):
    """Exception raised when a Git command fails"""
    pass


class GitCommand:
    """Base class for executing Git commands and handling output"""

    @staticmethod
    def run_command(command: List[str], cwd: Optional[str] = None) -> str:
        """Execute a git command and return its output"""
        try:
            # Convert any non-string arguments in the command to strings
            cmd = [str(arg) for arg in command]

            # For debugging
            # print(f"Running command: {' '.join(cmd)}")

            # Execute the command
            result = subprocess.run(
                cmd,
                cwd=cwd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                check=True,
                shell=False  # Ensure we're not using shell to avoid injection issues
            )
            return result.stdout.strip()
        except subprocess.CalledProcessError as e:
            # For debugging
            # print(f"Command failed: {e.stderr}")
            raise GitCommandError(f"Git command failed: {e.stderr}")

    @staticmethod
    def is_git_repo(path: str = '.') -> bool:
        """Check if the given path is a Git repository"""
        try:
            GitCommand.run_command(['git', 'rev-parse', '--is-inside-work-tree'], cwd=path)
            return True
        except GitCommandError:
            return False


@dataclass
class GitAuthor:
    """Represents a Git author or committer"""
    name: str
    email: str
    timestamp: Optional[datetime.datetime] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        result = {
            "name": self.name,
            "email": self.email
        }
        if self.timestamp:
            result["timestamp"] = self.timestamp.isoformat()
            result["timestamp_epoch"] = int(self.timestamp.timestamp())
        return result


@dataclass
class GitCommit:
    """Represents a Git commit with full metadata"""
    hash: str
    short_hash: str
    author: GitAuthor
    committer: GitAuthor
    message: str
    message_full: str  # Including body
    parent_hashes: List[str] = field(default_factory=list)
    tree_hash: str = ""
    files_changed: List[Dict[str, Any]] = field(default_factory=list)
    stats: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        return {
            "hash": self.hash,
            "short_hash": self.short_hash,
            "author": self.author.to_dict(),
            "committer": self.committer.to_dict(),
            "message": self.message,
            "message_full": self.message_full,
            "parent_hashes": self.parent_hashes,
            "tree_hash": self.tree_hash,
            "files_changed": self.files_changed,
            "stats": self.stats
        }


@dataclass
class GitFile:
    """Represents a Git file with its status"""
    path: str
    status: str  # 'tracked', 'untracked', 'modified', 'deleted', etc.
    staged: bool = False

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        return {
            "path": self.path,
            "status": self.status,
            "staged": self.staged
        }


@dataclass
class GitBranch:
    """Represents a Git branch"""
    name: str
    is_current: bool
    remote: Optional[str] = None
    upstream: Optional[str] = None
    upstream_status: Optional[Dict[str, int]] = None  # ahead/behind counts

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        return {
            "name": self.name,
            "is_current": self.is_current,
            "remote": self.remote,
            "upstream": self.upstream,
            "upstream_status": self.upstream_status
        }


@dataclass
class GitSubmodule:
    """Represents a Git submodule"""
    name: str
    path: str
    url: str
    branch: Optional[str] = None
    commit_hash: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        return {
            "name": self.name,
            "path": self.path,
            "url": self.url,
            "branch": self.branch,
            "commit_hash": self.commit_hash
        }


@dataclass
class GitTag:
    """Represents a Git tag"""
    name: str
    commit_hash: str
    annotated: bool = False
    message: Optional[str] = None
    tagger: Optional[GitAuthor] = None

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        result = {
            "name": self.name,
            "commit_hash": self.commit_hash,
            "annotated": self.annotated,
        }
        if self.message:
            result["message"] = self.message
        if self.tagger:
            result["tagger"] = self.tagger.to_dict()
        return result


@dataclass
class GitStatus:
    """Represents the current Git repository status"""
    branch: str
    files: List[GitFile] = field(default_factory=list)
    staged_files: int = 0
    changed_files: int = 0
    untracked_files: int = 0

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary format"""
        return {
            "branch": self.branch,
            "files": [f.to_dict() for f in self.files],
            "staged_files": self.staged_files,
            "changed_files": self.changed_files,
            "untracked_files": self.untracked_files
        }


class GitLog:
    """Extract and process Git log information"""

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

    def get_commits(self, max_count: Optional[int] = None,
                    skip: int = 0,
                    branch: Optional[str] = None,
                    author: Optional[str] = None,
                    since: Optional[str] = None,
                    until: Optional[str] = None,
                    include_stats: bool = False,
                    include_diff: bool = False) -> List[GitCommit]:
        """
        Retrieve git commits with filters

        Args:
            max_count: Maximum number of commits to retrieve
            skip: Number of commits to skip
            branch: Branch to get commits from
            author: Filter by author
            since: Get commits since date
            until: Get commits until date
            include_stats: Include file change statistics
            include_diff: Include commit diff

        Returns:
            List of GitCommit objects
        """
        # Use a format string with ISO-format dates instead of timestamps,
        # and don't include --stat in the initial command
        cmd = ['git', 'log', '--pretty=format:%H%n%h%n%an%n%ae%n%aI%n%cn%n%ce%n%cI%n%P%n%T%n%s%n%B%n--GIT_METADATA_DELIMITER--']

        if max_count:
            cmd.extend(['-n', str(max_count)])
        if skip:
            cmd.extend(['--skip', str(skip)])
        if branch:
            cmd.append(branch)
        if author:
            cmd.extend(['--author', author])
        if since:
            cmd.extend(['--since', since])
        if until:
            cmd.extend(['--until', until])

        # We'll get stats separately to avoid parsing issues
        # Don't add --stat to the initial command

        output = GitCommand.run_command(cmd, cwd=self.repo_path)
        commits = []

        commit_parts = output.split('--GIT_METADATA_DELIMITER--')
        for commit_part in commit_parts:
            if not commit_part.strip():
                continue

            lines = commit_part.strip().split('\n')
            if len(lines) < 12:  # Need at least this many fields
                continue

            commit_hash = lines[0]
            short_hash = lines[1]
            author_name = lines[2]
            author_email = lines[3]

            # Parse ISO 8601 date format for author timestamp
            try:
                author_timestamp = datetime.datetime.fromisoformat(lines[4].strip())
                author_time = int(author_timestamp.timestamp())
            except (ValueError, IndexError):
                author_timestamp = datetime.datetime.now()
                author_time = int(author_timestamp.timestamp())

            committer_name = lines[5]
            committer_email = lines[6]

            # Parse ISO 8601 date format for committer timestamp
            try:
                committer_timestamp = datetime.datetime.fromisoformat(lines[7].strip())
                committer_time = int(committer_timestamp.timestamp())
            except (ValueError, IndexError):
                committer_timestamp = datetime.datetime.now()
                committer_time = int(committer_timestamp.timestamp())

            # More defensive parsing for the remaining fields
            parent_hashes = lines[8].split() if len(lines) > 8 and lines[8] else []
            tree_hash = lines[9] if len(lines) > 9 else ""
            message_subject = lines[10] if len(lines) > 10 else ""
            message_full = '\n'.join(lines[11:]) if len(lines) > 11 else ""

            # Stats are usually after the message
            stats = {}
            files_changed = []

            if include_stats:
                try:
                    # Get stats separately using a specific git show command
                    stat_cmd = ['git', 'show', '--stat', '--format=', commit_hash]
                    commit_stats = GitCommand.run_command(stat_cmd, cwd=self.repo_path)

                    # Process stats (simplified)
                    stats_lines = commit_stats.strip().split('\n')
                    for line in stats_lines:
                        if '|' in line and line.strip():
                            parts = line.split('|')
                            if len(parts) >= 2:
                                file_path = parts[0].strip()
                                stats_info = parts[1].strip()

                                # Only add if it looks like a file stats line
                                if stats_info and any(c in stats_info for c in '+-'):
                                    files_changed.append({
                                        "file": file_path,
                                        "stats": stats_info
                                    })

                    # Find the summary line
                    for line in stats_lines:
                        if 'file' in line and 'changed' in line:
                            parts = line.split(',')
                            for part in parts:
                                part = part.strip()
                                if 'file' in part and 'changed' in part:
                                    try:
                                        stats['files_changed'] = int(part.split(' ')[0])
                                    except (ValueError, IndexError):
                                        pass
                                elif 'insertion' in part:
                                    try:
                                        stats['insertions'] = int(part.split(' ')[0])
                                    except (ValueError, IndexError):
                                        pass
                                elif 'deletion' in part:
                                    try:
                                        stats['deletions'] = int(part.split(' ')[0])
                                    except (ValueError, IndexError):
                                        pass
                except GitCommandError as e:
                    # If there's an error getting stats, just continue without them
                    print(f"Warning: Could not get stats for commit {commit_hash}: {e}")

            # Create author and committer objects with timestamps
            author = GitAuthor(
                name=author_name,
                email=author_email,
                timestamp=datetime.datetime.fromtimestamp(author_time)
            )

            committer = GitAuthor(
                name=committer_name,
                email=committer_email,
                timestamp=datetime.datetime.fromtimestamp(committer_time)
            )

            commit = GitCommit(
                hash=commit_hash,
                short_hash=short_hash,
                author=author,
                committer=committer,
                message=message_subject,
                message_full=message_full,
                parent_hashes=parent_hashes,
                tree_hash=tree_hash,
                files_changed=files_changed,
                stats=stats
            )

            commits.append(commit)

        return commits

    def to_json(self, commits: List[GitCommit], indent: int = 2) -> str:
        """Convert commits to JSON string"""
        return json.dumps([commit.to_dict() for commit in commits], indent=indent)

    def to_file(self, filename: str, commits: List[GitCommit], indent: int = 2) -> None:
        """Save commits to a JSON file"""
        with open(filename, 'w') as f:
            json.dump([commit.to_dict() for commit in commits], f, indent=indent)

class GitStatus:
    """Get and process current Git repository status"""

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

    def get_current_branch(self) -> str:
        """Get the name of the current branch"""
        return GitCommand.run_command(['git', 'rev-parse', '--abbrev-ref', 'HEAD'], cwd=self.repo_path)

    def get_status(self) -> Dict[str, Any]:
        """Get detailed repository status"""
        # Get status in porcelain format for parsing
        status_output = GitCommand.run_command(['git', 'status', '--porcelain', '-z'], cwd=self.repo_path)

        # Tracked files with changes (modified, deleted, etc)
        tracked_modified = []
        # New files staged for commit
        tracked_new = []
        # Untracked files
        untracked = []

        if status_output:
            entries = status_output.split('\0')
            for entry in entries:
                if not entry:
                    continue

                status_code = entry[:2]
                file_path = entry[3:]

                # First letter is status in staging area, second letter is status in working tree
                staged_status = status_code[0]
                working_status = status_code[1]

                # Untracked files
                if status_code == '??':
                    untracked.append(GitFile(path=file_path, status='untracked', staged=False))
                # New files
                elif staged_status == 'A':
                    tracked_new.append(GitFile(path=file_path, status='new', staged=True))
                # Modified files
                elif staged_status == 'M' or working_status == 'M':
                    tracked_modified.append(GitFile(
                        path=file_path,
                        status='modified',
                        staged=(staged_status == 'M')
                    ))
                # Deleted files
                elif staged_status == 'D' or working_status == 'D':
                    tracked_modified.append(GitFile(
                        path=file_path,
                        status='deleted',
                        staged=(staged_status == 'D')
                    ))
                # Renamed files
                elif staged_status == 'R':
                    tracked_modified.append(GitFile(
                        path=file_path,
                        status='renamed',
                        staged=True
                    ))
                # Other changes
                else:
                    tracked_modified.append(GitFile(
                        path=file_path,
                        status='changed',
                        staged=(staged_status != ' ')
                    ))

        # Create the status object
        status = {
            "branch": self.get_current_branch(),
            "tracked_modified": [file.to_dict() for file in tracked_modified],
            "tracked_new": [file.to_dict() for file in tracked_new],
            "untracked": [file.to_dict() for file in untracked],
            "has_staged_changes": any(file.staged for file in tracked_modified + tracked_new),
            "has_unstaged_changes": any(not file.staged for file in tracked_modified),
            "has_untracked_files": bool(untracked)
        }

        return status

    def to_json(self, indent: int = 2) -> str:
        """Get status as JSON string"""
        return json.dumps(self.get_status(), indent=indent)


class GitSubmodules:
    """Get and process Git submodule information"""

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

    def get_submodules(self) -> List[GitSubmodule]:
        """Get all submodules in the repository"""
        try:
            output = GitCommand.run_command(['git', 'submodule', 'status'], cwd=self.repo_path)
            submodules = []

            if not output:
                return submodules

            # Parse submodule status output
            for line in output.split('\n'):
                if not line.strip():
                    continue

                parts = line.strip().split()
                if len(parts) >= 2:
                    commit_hash = parts[0].lstrip('+-')
                    path = parts[1]

                    # Get submodule URL from .gitmodules file
                    url_cmd = ['git', 'config', '--file', '.gitmodules',
                               f'submodule.{path}.url']

                    try:
                        url = GitCommand.run_command(url_cmd, cwd=self.repo_path)
                    except GitCommandError:
                        url = "Unknown"

                    # Get branch if available
                    branch_cmd = ['git', 'config', '--file', '.gitmodules',
                                  f'submodule.{path}.branch']

                    try:
                        branch = GitCommand.run_command(branch_cmd, cwd=self.repo_path)
                    except GitCommandError:
                        branch = None

                    submodule = GitSubmodule(
                        name=os.path.basename(path),
                        path=path,
                        url=url,
                        branch=branch,
                        commit_hash=commit_hash
                    )

                    submodules.append(submodule)

            return submodules

        except GitCommandError:
            # No submodules or error
            return []

    def to_json(self, indent: int = 2) -> str:
        """Get submodules as JSON string"""
        submodules = self.get_submodules()
        return json.dumps([submodule.to_dict() for submodule in submodules], indent=indent)


class GitBranches:
    """Get and process Git branch information"""

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

    def get_branches(self, include_remote: bool = True) -> List[GitBranch]:
        """
        Get all branches in the repository

        Args:
            include_remote: Whether to include remote branches

        Returns:
            List of GitBranch objects
        """
        cmd = ['git', 'branch', '--verbose']
        if include_remote:
            cmd.append('--all')

        output = GitCommand.run_command(cmd, cwd=self.repo_path)
        branches = []

        current_branch = self.get_current_branch()

        for line in output.split('\n'):
            if not line.strip():
                continue

            # Check if this is the current branch
            is_current = line.startswith('*')
            branch_info = line[2:].strip()

            # Handle remote branches
            if ' -> ' in branch_info:
                # This is a symbolic ref, skip it
                continue

            parts = branch_info.split()
            if not parts:
                continue

            name = parts[0]
            commit_hash = parts[1] if len(parts) > 1 else None

            # Determine if this is a remote branch
            remote = None
            if '/' in name and name.startswith('remotes/'):
                name_parts = name.split('/')
                remote = name_parts[1]
                name = '/'.join(name_parts[2:])

            # Get tracking information if available
            upstream = None
            upstream_status = None

            if not remote and name == current_branch:
                try:
                    upstream_output = GitCommand.run_command(
                        ['git', 'rev-list', '--left-right', '--count', f'{name}...@{{upstream}}'],
                        cwd=self.repo_path
                    )

                    if upstream_output:
                        ahead, behind = upstream_output.split()
                        upstream_status = {
                            "ahead": int(ahead),
                            "behind": int(behind)
                        }

                    upstream_branch = GitCommand.run_command(
                        ['git', 'rev-parse', '--abbrev-ref', f'{name}@{{upstream}}'],
                        cwd=self.repo_path
                    )

                    if upstream_branch and '/' in upstream_branch:
                        upstream = upstream_branch

                except GitCommandError:
                    # No upstream set
                    pass

            branch = GitBranch(
                name=name,
                is_current=(name == current_branch),
                remote=remote,
                upstream=upstream,
                upstream_status=upstream_status
            )

            branches.append(branch)

        return branches

    def get_current_branch(self) -> str:
        """Get the name of the current branch"""
        try:
            return GitCommand.run_command(['git', 'rev-parse', '--abbrev-ref', 'HEAD'], cwd=self.repo_path)
        except GitCommandError:
            return "HEAD"

    def to_json(self, include_remote: bool = True, indent: int = 2) -> str:
        """Get branches as JSON string"""
        branches = self.get_branches(include_remote=include_remote)
        return json.dumps([branch.to_dict() for branch in branches], indent=indent)


class GitTags:
    """Get and process Git tag information"""

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

    def get_tags(self) -> List[GitTag]:
        """Get all tags in the repository"""
        # Get basic tag info
        output = GitCommand.run_command(['git', 'tag', '-l'], cwd=self.repo_path)
        tags = []

        if not output:
            return tags

        tag_names = output.strip().split('\n')

        for tag_name in tag_names:
            if not tag_name.strip():
                continue

            # Get the commit hash for this tag
            commit_hash = GitCommand.run_command(
                ['git', 'rev-list', '-n', '1', tag_name],
                cwd=self.repo_path
            )

            # Check if this is an annotated tag
            try:
                show_output = GitCommand.run_command(
                    ['git', 'show', '-s', '--format=%an%n%ae%n%at%n%s', tag_name],
                    cwd=self.repo_path
                )

                if show_output:
                    lines = show_output.strip().split('\n')
                    if len(lines) >= 4:
                        annotated = True
                        tagger_name = lines[0]
                        tagger_email = lines[1]
                        tagger_time = int(lines[2])
                        tag_message = lines[3]

                        tagger = GitAuthor(
                            name=tagger_name,
                            email=tagger_email,
                            timestamp=datetime.datetime.fromtimestamp(tagger_time)
                        )
                    else:
                        annotated = False
                        tagger = None
                        tag_message = None
                else:
                    annotated = False
                    tagger = None
                    tag_message = None

            except GitCommandError:
                annotated = False
                tagger = None
                tag_message = None

            tag = GitTag(
                name=tag_name,
                commit_hash=commit_hash,
                annotated=annotated,
                message=tag_message,
                tagger=tagger
            )

            tags.append(tag)

        return tags

    def to_json(self, indent: int = 2) -> str:
        """Get tags as JSON string"""
        tags = self.get_tags()
        return json.dumps([tag.to_dict() for tag in tags], indent=indent)


class GitRepository:
    """
    Main class for working with Git repositories
    Provides access to all Git metadata types
    """

    def __init__(self, repo_path: str = '.'):
        self.repo_path = repo_path
        if not GitCommand.is_git_repo(repo_path):
            raise GitCommandError(f"Not a git repository: {repo_path}")

        # Initialize components
        self.log = GitLog(repo_path)
        self.status = GitStatus(repo_path)
        self.branches = GitBranches(repo_path)
        self.tags = GitTags(repo_path)
        self.submodules = GitSubmodules(repo_path)

    def get_repo_info(self) -> Dict[str, Any]:
        """Get comprehensive repository information"""
        return {
            "path": os.path.abspath(self.repo_path),
            "remotes": self._get_remotes(),
            "current_branch": self.branches.get_current_branch(),
            "head_commit": self._get_head_commit().to_dict(),
            "has_changes": self._has_changes(),
            "stats": self._get_repo_stats()
        }

    def _get_remotes(self) -> Dict[str, Dict[str, str]]:
        """Get all remotes configured for the repository"""
        remotes = {}

        remote_output = GitCommand.run_command(['git', 'remote', '-v'], cwd=self.repo_path)
        if not remote_output:
            return remotes

        for line in remote_output.split('\n'):
            if not line.strip():
                continue

            parts = line.split()
            if len(parts) >= 2:
                name = parts[0]
                url = parts[1]
                remote_type = parts[2].strip('()')

                if name not in remotes:
                    remotes[name] = {}

                remotes[name][remote_type] = url

        return remotes

    def _get_head_commit(self) -> GitCommit:
        """Get the HEAD commit"""
        commits = self.log.get_commits(max_count=1)
        if commits:
            return commits[0]
        else:
            # Empty repository, return placeholder
            author = GitAuthor(name="", email="", timestamp=None)
            return GitCommit(
                hash="",
                short_hash="",
                author=author,
                committer=author,
                message="",
                message_full=""
            )

    def _has_changes(self) -> bool:
        """Check if the repository has uncommitted changes"""
        status = self.status.get_status()
        return (status["has_staged_changes"] or
                status["has_unstaged_changes"] or
                status["has_untracked_files"])

    def _get_repo_stats(self) -> Dict[str, Any]:
        """Get repository statistics"""
        try:
            # Count commits
            commit_count = GitCommand.run_command(
                ['git', 'rev-list', '--count', 'HEAD'],
                cwd=self.repo_path
            )

            # Count files
            file_count = GitCommand.run_command(
                ['git', 'ls-files', '--exclude-standard', '|', 'wc', '-l'],
                cwd=self.repo_path
            )

            # Get first commit date
            first_commit_date = GitCommand.run_command(
                ['git', 'log', '--reverse', '--format=%at', '|', 'head', '-1'],
                cwd=self.repo_path
            )

            return {
                "commit_count": int(commit_count.strip()),
                "file_count": int(file_count.strip()),
                "first_commit": int(first_commit_date.strip()) if first_commit_date.strip() else None,
                "branch_count": len(self.branches.get_branches(include_remote=False)),
                "tag_count": len(self.tags.get_tags()),
                "submodule_count": len(self.submodules.get_submodules())
            }

        except GitCommandError:
            return {}

    def to_json(self, indent: int = 2) -> str:
        """Get repository information as JSON string"""
        return json.dumps(self.get_repo_info(), indent=indent)

    def get_full_metadata(self) -> Dict[str, Any]:
        """
        Get comprehensive metadata for the repository

        This includes:
        - Repository information
        - Current status
        - Branches
        - Tags
        - Submodules
        - Recent commits

        Returns:
            Dictionary with all metadata
        """
        return {
            "repo_info": self.get_repo_info(),
            "status": self.status.get_status(),
            "branches": [branch.to_dict() for branch in self.branches.get_branches()],
            "tags": [tag.to_dict() for tag in self.tags.get_tags()],
            "submodules": [submodule.to_dict() for submodule in self.submodules.get_submodules()],
            "recent_commits": [commit.to_dict() for commit in self.log.get_commits(max_count=10)]
        }


# Example usage
if __name__ == "__main__":
    # Create a repository object
    repo = GitRepository()

    # Get and print repository information
    print("Repository Information:")
    print(repo.to_json())

    # Get and print current status
    print("\nCurrent Status:")
    print(repo.status.to_json())

    # Get and print branches
    print("\nBranches:")
    print(repo.branches.to_json())

    # Get and print recent commits
    print("\nRecent Commits:")
    commits = repo.log.get_commits(max_count=5, include_stats=True)
    print(repo.log.to_json(commits))

    # Get and print submodules
    print("\nSubmodules:")
    print(repo.submodules.to_json())

    # Get and print tags
    print("\nTags:")
    print(repo.tags.to_json())

    # Get and save full metadata
    print("\nSaving full metadata to git_metadata.json")
    with open("git_metadata.json", "w") as f:
        json.dump(repo.get_full_metadata(), f, indent=2)