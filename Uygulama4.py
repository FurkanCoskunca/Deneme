import sys
import os
import pefile
def clean_section_name(section_name):
    section_name = section_name.decode(errors='ignore')  # Byte'ları stringe dönüştür, hataları yoksay
    section_name = section_name.strip('\0')  # Null byte'ları sil
    return section_name.strip()   # Baştaki ve sondaki boşlukları sil
target_folder = sys.argv[1]  # Terminalde yazılacak olan klasör adı(ilk parametre)
target_expression = sys.argv[2].encode()   # Terminalde klasör adından sonra aranacak olan ifade(ikinci parametre)
def confirmMachine(filePath): #Bu fonksiyon headerleri kontrol ederek makine tipinin istenen tiplerde olup olmadığına bakar.
    valid_machine_types = [
        pefile.MACHINE_TYPE["IMAGE_FILE_MACHINE_AMD64"],
        pefile.MACHINE_TYPE["IMAGE_FILE_MACHINE_I386"]
    ]

    try:
        pe = pefile.PE(filePath)
        if pe.FILE_HEADER.Machine in valid_machine_types:
            return True
        else:
            return False
    except pefile.PEFormatError:
        return False
def search_malware(file_path):
    try:
        with open(file_path, "rb") as file:
            pe_data = file.read() #Dosya okunuyor ve PE dosyası verisi pe_verisi değişkenine aktarıldı.
        pe = pefile.PE(data=pe_data)  #pefile modülüyle PE dosyası oluşturuldu.
        section_sizes = []   #Bölüm boyutlarını tutması için bir liste.
        total_offset = 0  #Toplam offseti tutması için bir değişken oluşturuldu ve 0 değeri atıldı.
        start_offset = pe.sections[0].PointerToRawData   #İlk bölümün başlangıç offseti belirlendi.
        for section in pe.sections:  #PE dosyasındaki her bir bölüm anlamına gelir.
            section_sizes.append(section.SizeOfRawData)  #Bölüm boyutu bölüm_boyutları listesine eklendi.
            section_data = pe_data[section.PointerToRawData : section.PointerToRawData + section.SizeOfRawData] #Bölüm verisi, PE dosyasındaki ilgili konuma (PointerToRawData) göre alındı.
            term_offset = section_data.find(target_expression) #Aranacak ifade, bölüm verisinde bulunmaya çalışılıyor. Eğer bulunursa bulunan ifadenin ofseti (terim_ofseti) kaydedilir.
            if term_offset != -1:
                section_name = clean_section_name(section.Name)  #Bölüm adı temizleniyor ve bölüm_temizle fonksiyonuyla düzenleniyor.
                total_offset += sum(section_sizes[:-1]) + term_offset  #Toplam ofset hesaplanıyor, bu ofset sonucu, bölüm başlangıç ofsetine eklenerek gerçek bellek adresi bulunuyor.Bulunan sonuç bir dize olarak döndürülüyor.
                return f"BULUNDU ({ start_offset +  total_offset} adresinde) ve {section_name} bölümü içerisinde"
        return "BULUNAMADI."  #Eğer hiçbir bölümde aranan ifade bulunamazsa "BULUNAMADI." mesajı
    except (IOError, pefile.PEFormatError) as error:   #Dosya okumayla veya PE dosyası formatıyla ilgili bir hata oluşursa hata mesajı döner.
        if isinstance(error, pefile.PEFormatError):
            return "Geçerli PE dosyası değil."
        return str(error)
def main():
    file_paths = []  #Bir liste oluşturuldu.
    for root, directories, files in os.walk(target_folder):    #Taranacak klasördeki tüm alt dizinler ve dosyalar dolaşılıyor.
        for file_name in files:
            file_path = os.path.join(root, file_name)
            file_paths.append(file_path)   #Her bir dosya için, dosya yolu oluşturuldu ve dosya_yolları listesine eklendi.
    for file_path in file_paths:
        result = search_malware(file_path)
        print(f"{file_path} {result}")   #dosya_yolları listesindeki her bir dosya için malware_ara fonksiyonu çağrılıyor ve sonuç ekrana yazdırılıyor.
if __name__ == "__main__":
    main()
