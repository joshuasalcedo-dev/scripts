import Image from 'next/image';
interface IconMap {
    [key: string]: string;
}

// Map of icon names to their file paths in the public directory
const iconMap: IconMap = {
    sh: '/icons/bash.svg',
    css: '/icons/css.svg',
    docker: '/icons/docker.svg',
    document: '/icons/document.svg',
    file: '/icons/file.svg',
    folder: '/icons/folder.svg',
    git: '/icons/git.svg',
    github: '/icons/github.svg',
    html5: '/icons/html-5.svg',
    imageFile: '/icons/image-file.svg',
    intellijIdea: '/icons/intellij-idea.svg',
    java: '/icons/java.svg',
    javascript: '/icons/javascript.svg',
    lock: '/icons/lock.svg',
    openedFolder: '/icons/opened-folder.svg',
    postgresql: '/icons/postgresql.svg',
    powershell: '/icons/powershell.svg',
    python: '/icons/python.svg',
    script: '/icons/script.svg',
    search: '/icons/search.svg',
    thymeleaf: '/icons/thymeleaf.svg',
    typescript: '/icons/typescript.svg',
    jwt: '/icons/jwt.svg',
    xml: '/icons/xml.svg',
    yaml: '/icons/yaml.svg',
    mongodb: '/icons/mongodb.svg',
};

export default iconMap;

export const iconMapKeys = Object.keys(iconMap);


export function DirectoryIcon(props: { name: string }) {
    return (
        <div>
            <Image
                src={iconMap[props.name] || iconMap.file} // Use the provided name or fallback to default file icon
                alt={`${props.name} Icon`}
                width={24}
                height={24}
            />
        </div>
    );
}