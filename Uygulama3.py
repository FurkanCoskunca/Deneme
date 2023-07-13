import os
import sys

def search_word(folder, target_word):
    for file_name in os.listdir(folder):
        file_path = os.path.join(folder, file_name)
        if os.path.isfile(file_path):
            with open(file_path, 'r') as file:
                content = file.read()
                if target_word in content:
                    print(f"{file_path}: BULUNDU ({os.path.getsize(file_path)} adresinde)")
                else:
                    print(f"{file_path}: BULUNAMADI")
        elif os.path.isdir(file_path):
            search_word(file_path, target_word)

folder = sys.argv[1]
target_word = sys.argv[2]

search_word(folder, target_word)
