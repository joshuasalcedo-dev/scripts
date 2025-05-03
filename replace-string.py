import argparse
import os
import sys

def parse_args():
    parser = argparse.ArgumentParser(description='Replace text in a file interactively.')
    parser.add_argument('file_path', type=str, help='Path to the file to modify')
    return parser.parse_args()

def replace_text_in_file(file_path, old_text, new_text):
    try:
        # Check if file exists
        if not os.path.isfile(file_path):
            print(f"Error: File '{file_path}' does not exist.")
            return False

        # Read the file content
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()

        # Check if the text exists in the file
        if old_text not in content:
            print(f"Warning: Text '{old_text}' not found in the file.")
            return False

        # Replace the text
        new_content = content.replace(old_text, new_text)

        # Write the modified content back to the file
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(new_content)

        print(f"Successfully replaced '{old_text}' with '{new_text}' in {file_path}")
        return True

    except Exception as e:
        print(f"Error: {str(e)}")
        return False

def main():
    args = parse_args()
    file_path = args.file_path

    # Validate file path
    if not os.path.exists(file_path):
        print(f"Error: The specified file '{file_path}' does not exist.")
        sys.exit(1)

    # Interactive prompt for text to find
    find_text = input("Enter the text to find: ")
    if not find_text:
        print("Error: Find text cannot be empty.")
        sys.exit(1)

    # Interactive prompt for replacement text
    replace_text = input("Enter the text to replace with: ")

    # Confirm operation
    print(f"\nReady to replace '{find_text}' with '{replace_text}' in file: {file_path}")
    confirm = input("Proceed? (y/n): ").lower()

    if confirm == 'y' or confirm == 'yes':
        success = replace_text_in_file(file_path, find_text, replace_text)
        if success:
            print("Replacement operation completed successfully.")
        else:
            print("Replacement operation failed.")
    else:
        print("Operation cancelled.")

if __name__ == "__main__":
    main()