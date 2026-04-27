#!/bin/bash
# Bookstore API demo - shows the output of every operation.
# Run after starting the app:  mvn spring-boot:run

BASE="http://localhost:8080/api/v1"

echo "========================================"
echo "1. LOGIN AS ADMIN"
echo "========================================"
ADMIN_TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "Admin token acquired: ${ADMIN_TOKEN:0:30}..."

echo ""
echo "========================================"
echo "2. LOGIN AS USER"
echo "========================================"
USER_TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "User token acquired: ${USER_TOKEN:0:30}..."

echo ""
echo "========================================"
echo "3. UNAUTHENTICATED REQUEST (expect 401)"
echo "========================================"
curl -s -i "$BASE/books" | head -n 1

echo ""
echo "========================================"
echo "4. SEARCH ALL BOOKS (as user)"
echo "========================================"
curl -s "$BASE/books" -H "Authorization: Bearer $USER_TOKEN" | python3 -m json.tool

echo ""
echo "========================================"
echo "5. ADD A NEW BOOK (as user)"
echo "========================================"
curl -s -X POST "$BASE/books" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9780553103540",
    "title": "A Game of Thrones",
    "authors": [{"name":"George R.R. Martin","birthday":"1948-09-20"}],
    "year": 1996,
    "price": 25.00,
    "genre": "Fantasy"
  }' | python3 -m json.tool

echo ""
echo "========================================"
echo "6. ADD DUPLICATE (expect 409)"
echo "========================================"
curl -s -X POST "$BASE/books" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9780553103540",
    "title": "Duplicate",
    "authors": [{"name":"Someone","birthday":"1900-01-01"}],
    "year": 2020, "price": 10.00, "genre": "Fiction"
  }' | python3 -m json.tool

echo ""
echo "========================================"
echo "7. UPDATE A BOOK"
echo "========================================"
curl -s -X PUT "$BASE/books/9780553103540" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9780553103540",
    "title": "A Game of Thrones (Special Edition)",
    "authors": [{"name":"George R.R. Martin","birthday":"1948-09-20"}],
    "year": 1996,
    "price": 29.99,
    "genre": "Fantasy"
  }' | python3 -m json.tool

echo ""
echo "========================================"
echo "8. SEARCH BY EXACT TITLE"
echo "========================================"
curl -s "$BASE/books?title=1984" \
  -H "Authorization: Bearer $USER_TOKEN" | python3 -m json.tool

echo ""
echo "========================================"
echo "9. SEARCH BY EXACT AUTHOR"
echo "========================================"
curl -s "$BASE/books?author=George%20Orwell" \
  -H "Authorization: Bearer $USER_TOKEN" | python3 -m json.tool

echo ""
echo "========================================"
echo "10. SEARCH BY TITLE + AUTHOR"
echo "========================================"
curl -s "$BASE/books?title=Good%20Omens&author=Neil%20Gaiman" \
  -H "Authorization: Bearer $USER_TOKEN" | python3 -m json.tool

echo ""
echo "========================================"
echo "11. INVALID BOOK (expect 400 with details)"
echo "========================================"
curl -s -X POST "$BASE/books" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"isbn":"bad","title":"","authors":[],"year":1000,"price":-5,"genre":""}' \
  | python3 -m json.tool

echo ""
echo "========================================"
echo "12. USER TRIES TO DELETE (expect 403)"
echo "========================================"
curl -s -i -X DELETE "$BASE/books/9780451524935" \
  -H "Authorization: Bearer $USER_TOKEN" | head -n 1

echo ""
echo "========================================"
echo "13. ADMIN DELETES A BOOK (expect 204)"
echo "========================================"
curl -s -i -X DELETE "$BASE/books/9780553103540" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | head -n 1

echo ""
echo "========================================"
echo "14. DELETE NON-EXISTENT BOOK (expect 404)"
echo "========================================"
curl -s -X DELETE "$BASE/books/9999999999999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -m json.tool

echo ""
echo "Done!"
