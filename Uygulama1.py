import sys
if len(sys.argv) > 1:
    file_path = sys.argv[1]
    with open(file_path, "rb") as file:
        first_two_bytes = file.read(2)
        decoded_bytes = first_two_bytes.decode("utf-8") #b'nin gitmesi için decode işlemi.
        print("Dosyanın ilk iki baytı:",decoded_bytes)

