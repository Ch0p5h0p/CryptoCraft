# CryptoCraft

A simple cryptography mod for Minecraft (fabric)

CryptoCraft includes:
1. Asymmetric encryption and decryption
2. Symmetric encryption and decryption
3. Item hashing (hashes the full NBT of the item)
4. Signature generation and verification
5. GZip string compression

## Algorithms:
- Private/Public key: RSA-1024
- Symmetric encryption:  AES-256-CBC
- Hashing: SHA-256

## Soft requirements:
- ChatPatches (for copying chat messages)
- SlashLength (to enable long commands)

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

## Future implementations
- Vigenere cipher
- Ceasar cipher
- XOR cipher
- Book chunking: chunks text into sizes able to be pasted into books
- Book reader: reads all the pages in a book and concatenates them into one string
