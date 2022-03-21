#pragma once
#include "HashNode.h"
#include <fstream>
#include <string>

const int TABLE_SIZE = 200000;

class HashMap {
private:
	HashNode* htable[TABLE_SIZE];
	
	// Private utility functions
	int HashFunc(const std::string& key) const;
	std::string To_lower(const std::string& s) const;
	int getMax(const int arr[], int n) const;
	void radixsort(int arr[], int n);
	void countSort(int arr[], int n, int exp);
	void convertToString(char* arry, const int arr[], int len);
	void convertToASCII(const std::string& letter, int arr[]);

public:
	HashMap();
	~HashMap();

	// Delete copy constructor and assignment operator
	HashMap(const HashMap&) = delete;
	HashMap& operator=(const HashMap&) = delete;

	std::string SortWord(const std::string& w);
	void Insert(const std::string& key, const std::string& word);
	void search(const std::string& key, const std::string& word, std::ofstream& out) const;
	bool isWord(const std::string& s) const;
};
