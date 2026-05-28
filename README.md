# snip. — URL Shortener with Analytics

A full-stack URL shortener built with Spring Boot and Vanilla JS.  
Shorten long URLs, share them, and track click analytics in real time.

## Features

- Shorten any URL with a custom expiry (1–365 days)
- Base62 encoding for clean, short codes
- Click tracking — browser, OS, device, clicks per day
- Analytics dashboard with charts
- Async click logging using @Async (non-blocking redirects)
- Input validation with meaningful error messages

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3
- Spring Data JPA + Hibernate
- PostgreSQL
- Lombok

**Frontend**
- Vanilla JS
- Chart.js

**Caching**
- Redis

## Project Structure

```
src/
└── main/
    ├── java/com/url_shortner/
    │   ├── controller/        # REST endpoints
    │   ├── service/           # Business logic
    │   ├── repository/        # JPA repositories
    │   ├── entity/            # DB entities
    │   ├── dto/               # Request/Response DTOs
    │   └── config/            # Async + Web config
    └── resources/
        └── static/            # Frontend (index.html, analytics.html)
```

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL
- Maven

### Setup

1. Clone the repo
```bash
git clone https://github.com/yourusername/url-shortener.git
cd url-shortener
```

2. Create the database
```bash
psql -U your_username postgres
CREATE DATABASE urlshortner;
\q
```

3. Configure application.properties
```bash
cp src/main/resources/application-sample.properties src/main/resources/application.properties
```
Update with your DB credentials.

4. Run the app
```bash
./mvnw spring-boot:run
```

5. Open in browser
```
http://localhost:8080/index.html
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/shorten` | Shorten a URL |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/analytics/{shortCode}` | Get click analytics |

### POST /shorten

Request:
```json
{
  "longUrl": "https://example.com/very/long/url",
  "expiryDays": 30
}
```

Response:
```json
{
  "shortUrl": "http://localhost:8080/111",
  "shortCode": "111",
  "expiresAt": "2026-06-14T13:08:48"
}
```

## How It Works

1. User submits a long URL
2. Spring Boot saves it to PostgreSQL and gets an auto-generated ID
3. ID is encoded using Base62 to generate a short code
4. When someone visits the short URL, server responds with a 302 redirect
5. Click is logged asynchronously (non-blocking) with browser, OS, and device info parsed from the User-Agent header


## Future Improvements

- JWT authentication for private/protected URLs
- Rate limiting per IP to prevent abuse
- Custom alias support (e.g. snip/my-link)
- QR code generation for each short URL
- Geographic analytics (country breakdown)
