# CryptoCraft

A simple cryptography mod for Minecraft (fabric)

CryptoCraft includes:
- Asymmetric encryption and decryption
- Symmetric encryption and decryption
- Item hashing (hashes the full NBT of the item)
- Signature generation and verification
- GZip string compression
- Book text concatenation
- Text chunking for books

## Algorithms:
- Private/Public key: RSA-1024
- Symmetric encryption:  AES-256-CBC
- Hashing: SHA-256

## Soft requirements:
- ChatPatches (for copying chat messages)
- Slash Length (to enable long commands)

## Notes on the algorithms:
- RSA-1024: Yes, I know RSA isn't as good as ECC. Yes, I know that 1024 is a horrible key size. However: RSA means I dont have to also track signing vs encrypting keys, and 1024 key length means that the keys are shorter and easier to put in chat (or books, potentially)
- AES-256-CBC: I found it slightly easier to implement CBC that GCM, so I went with that
- SHA-256: Why not? If I'm correct, it's the current standard

**Remember: this is an encryption mod for a block game. If you need to encrypt or sign real, serious information, use PGP.**

## Commands
- asym_encrypt & asym_decrypt: public key encryption
- sym_encrypt & sym_decrypt: symmetric encryption
- hashitem: hash the item in the main hand
- sign: private key sign text
- verify: verify a signature
- add_key: add a public key to your key registry
- get_key_fingerprints: list all registered public keys
- get_public: return your public key
- compress & decompress: compress and decompress text with GZip
- read_book: read all the text from a book and concatenate it into one string
- hash_book: automatically hash a book's contents
- chunk_text: chunk text into sizes viable for use in books

## Future implementations
- Vigenere cipher
- Caesar cipher
- XOR cipher

## Disclaimer
Yes, this is a cryptography mod. However, I am also a tired and ADHD college student that, while I enjoy crypto and coding, I get tired of trying to coerce Java into doing what I want. Also, I'm operating generally off of assumptions on what people are willing to work for. If someone is willing to spend huge amounts of resources trying to find out where you hid your diamonds, they were going to find them one way or another. Given what I've said, you're using this mod with two things in mind:
1. This is a cryptography mod, and will protect your communications from the average Joe.
2. This is not a "modern cryptography" mod. If someone like the NSA takes legitimate interest in your communications, this won't save you. Sorry :(