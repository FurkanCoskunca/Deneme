import sys

def search_malware(file_path, search_phrase):
    try:
        with open(file_path, 'rb') as file:
            content = file.read()
            offset = content.find(search_phrase.encode()) #search_phrase'in content içindeki konumunu buluyoruz.
            if offset != -1:
                print(f'"{search_phrase}" ifadesi {offset} adresinde bulundu.')
            else:
                print(f'"{search_phrase}" ifadesi herhangi bir konumda bulunamadı.')
    except FileNotFoundError:
        print("Dosya bulunamadı.")

# Dosya yolu ve aranacak ifade argüman olarak verilecek
file_path = sys.argv[1]
search_phrase = sys.argv[2]
search_malware(file_path, search_phrase)
