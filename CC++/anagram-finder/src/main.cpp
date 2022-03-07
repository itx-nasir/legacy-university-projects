#include "HashMap.h"
#include <filesystem>
#include <stdexcept>

void FileReading() {
	HashMap H;
	const std::string data_dir = "data/";
	const std::string output_dir = "output/";
	
	try {
		// Read dictionary
		std::ifstream Read(data_dir + "words.txt");
		if (!Read) {
			throw std::runtime_error("Unable to open file words.txt");
		}
		
		std::cout << "Reading Word.txt...";
		std::string word, sortedword;
		while (Read >> word) {
			if (H.isWord(word)) {
				sortedword = H.SortWord(word);
				H.Insert(sortedword, word);
			}
		}
		std::cout << "Done" << std::endl;
		Read.close();

		// Process input file
		std::ofstream Write(output_dir + "output.txt");
		if (!Write) {
			throw std::runtime_error("Unable to open file output.txt");
		}

		std::ifstream InputRead(data_dir + "input.txt");
		if (!InputRead) {
			throw std::runtime_error("Unable to open file input.txt");
		}

		std::string inputWord;
		while (std::getline(InputRead, inputWord)) {
			if (!inputWord.empty()) {
				sortedword = H.SortWord(inputWord);
				Write << inputWord << ": ";
				H.search(sortedword, inputWord, Write);
				Write << "\n";
			}
		}
		
		InputRead.close();
		Write.close();
	}
	catch (const std::exception& e) {
		std::cerr << "Error: " << e.what() << std::endl;
		exit(1);
	}
}

int main() {
	FileReading();
	return 0;
}