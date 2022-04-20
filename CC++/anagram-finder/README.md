# Anagram Finder

This project was developed as part of the Algorithm and Design course at University. It implements a hash table-based solution for finding anagrams of words efficiently.

## Project Structure
- `src/`: Contains all source code
  - `include/`: Header files
  - `implementation/`: CPP implementation files
- `data/`: Input data files
- `output/`: Generated output files

## Features
- Custom hash table implementation
- Efficient anagram finding using sorted word keys
- File-based input/output processing
- Case-insensitive word matching

## Building and Running
1. Compile the project:
   ```bash
   g++ src/main.cpp src/implementation/HashMap.cpp -I src/include -o anagram_finder
   ```
2. Place your dictionary in `data/words.txt`
3. Place your input words in `data/input.txt`
4. Run the program:
   ```bash
   ./anagram_finder
   ```

## Educational Purpose
This project was created for educational purposes as part of university coursework in Data Structures and Algorithms.
