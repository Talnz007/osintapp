# Talkwalker OSINT Hub – Backend Developer API Specification
**Target Audience**: Office Server Development (FastAPI / Next.js / Express / Flask)  
**Client**: OSINT Mobile Android App (Jetpack Compose) & Web Dashboard  
**Date**: September 2026

---

## 1. Authentication & Security
- **Auth Method**: HTTP Basic Auth (`Authorization: Basic <base64(user:pass)>`)
- **Default Dev Credentials**:
  - `Username`: `CANTTHINKOFNTHIN`
  - `Password`: `NOTFASCIST`
- **CORS**: Must allow all origins (`*`) or tunnel domain, with `Authorization` and `Content-Type` allowed in headers.

---

## 2. API Endpoints Specification

### 2.1. Authentication Verification
- **Endpoint**: `POST /api/login`
- **Headers**: `Authorization: Basic <credentials>`
- **Response (200 OK)**:
  ```json
  {
    "status": "authenticated",
    "user": "CANTTHINKOFNTHIN"
  }
  ```
- **Error (401 Unauthorized)**:
  ```json
  {
    "detail": "Invalid credentials"
  }
  ```

---

### 2.2. Overview & Threat Metrics
- **Endpoint**: `GET /api/stats`
- **Query Parameters**:
  - `hours` *(optional, int)*: Filter statistics by past N hours (e.g. `6`, `24`, `720`).
- **Response (200 OK)**:
  ```json
  {
    "total_posts": 12451,
    "total_comments": 13382,
    "total_hashtags": 15269,
    "total_mentions": 1758,
    "total_high_profile": 1263,
    "total_shaheed_incidents": 662,
    "estimated_reach": 191420,
    "posts_with_images": 7245,
    "top_hashtags": [
      { "hashtag": "#rightsmovementajk", "count": 3292 },
      { "hashtag": "#stopstateterrorism", "count": 547 },
      { "hashtag": "#balochistan", "count": 445 }
    ],
    "top_mentions": [
      { "mention": "@warmonitorpakaf", "count": 380 },
      { "mention": "@un", "count": 52 }
    ],
    "pipelines": [
      { "source_pipeline": "regional_news", "count": 7245 },
      { "source_pipeline": "sentiment_only", "count": 5206 }
    ],
    "categories": [
      { "category_label": "REGIONAL", "count": 5922 },
      { "category_label": "GLOBAL", "count": 1323 },
      { "category_label": "UNASSIGNED", "count": 5206 }
    ],
    "platforms": [
      { "platform": "twitter", "count": 12451 }
    ],
    "attack_types": [
      { "attack_type": "AMBUSH_FIRE", "count": 1300 },
      { "attack_type": "IED_EXPLOSIVE", "count": 341 },
      { "attack_type": "CIVIL_UNREST", "count": 212 },
      { "attack_type": "SABOTAGE", "count": 55 },
      { "attack_type": "GENERAL", "count": 10543 }
    ],
    "activity": [
      { "date": "2026-09-02", "count": 56 },
      { "date": "2026-09-01", "count": 229 }
    ]
  }
  ```

---

### 2.3. Live Intelligence Feed
- **Endpoint**: `GET /api/posts`
- **Query Parameters**:
  - `page` *(int, default 1)*: 1-indexed page number.
  - `limit` *(int, default 25)*: Items per page.
  - `category` *(optional, string)*: Filter by `REGIONAL`, `GLOBAL`, or `UNASSIGNED`.
  - `attack_type` *(optional, string)*: `AMBUSH_FIRE`, `IED_EXPLOSIVE`, `CIVIL_UNREST`, `SABOTAGE`, `GENERAL`.
  - `pipeline` *(optional, string)*: `regional_news` or `sentiment_only`.
  - `hours` *(optional, int)*: Filter posts created in last N hours (e.g. `6`, `24`).
  - `search` *(optional, string)*: Free-text query matching content, hashtags, or account handle.
- **Response (200 OK)**:
  ```json
  {
    "total": 12451,
    "page": 1,
    "limit": 25,
    "pages": 499,
    "data": [
      {
        "id": 630828,
        "url": "https://x.com/TBPEnglish/status/2095066147385233531",
        "platform": "twitter",
        "account": "TBPEnglish",
        "content": "Berlin Cultural Event Honours Banuk Karima and Baloch Women...",
        "scraped_date": "2026-09-02T08:27:48.000Z",
        "processed_at": "2026-09-02T09:13:39.878551+00:00",
        "source_pipeline": "regional_news",
        "category_id": 1,
        "category_label": "GLOBAL",
        "target_whatsapp_group": "Automated news",
        "whatsapp_message_sent": "AOA Sir,\n\n🔶 *Subj: OSINT Update – Berlin Diaspora Event Honouring Baloch Separatist Activist*\n\n🔷 **30 August 2026, Berlin**: A cultural-political programme organised by the **Yaagi Art Collective** honoured late **Banuk Karima**...",
        "had_image": 1,
        "sentiment_image_path": "/path/to/images/twitter_TBPEnglish_1788339470478.png",
        "delivery_status": "sent",
        "is_high_profile": 0,
        "is_shaheed_incident": 0,
        "attack_type": "GENERAL",
        "comments": [
          "Analysis of public replies"
        ],
        "hashtags": [
          "#RightsMovementAJK",
          "#Balochistan"
        ]
      }
    ]
  }
  ```

---

### 2.4. Daily OSINT Picture (Sitrep Data)
- **Endpoint**: `GET /api/osint-picture`
- **Query Parameters**:
  - `hours` *(optional, int)*: Window in hours (default 24).
- **Response (200 OK)**:
  ```json
  {
    "period_covering": "123000 MAY to 123000 MAY",
    "Prominent Social Media Posts": [
      {
        "id": 630831,
        "account": "#RightsMovementAJK",
        "content": "Text of top intercepted post...",
        "url": "https://x.com/...",
        "had_image": 1,
        "sentiment_image_path": "relative/or/full/path.png"
      }
    ],
    "Major Claims": [
      {
        "id": 630827,
        "account": "MilitantClaimTracker",
        "content": "Insurgent claim details...",
        "attack_type": "AMBUSH_FIRE",
        "url": "https://x.com/..."
      }
    ],
    "Trends": {
      "Counter Terrorism Trends": [
        { "topic": "Border skirmish Lki sector", "count": 82, "reach": 14200 },
        { "topic": "FAH IED claim Kech", "count": 45, "reach": 9800 }
      ],
      "General Trends": [
        { "topic": "AJK Rights Movement rallies", "count": 320, "reach": 58000 }
      ]
    },
    "OSINT Trends": [
      { "trend": "Anti-state diaspora campaign Berlin", "sentiment": "negative", "volume": 120 },
      { "trend": "Civilian infrastructure restoration", "sentiment": "positive", "volume": 85 }
    ],
    "Assessment": {
      "Points": [
        "FAK proj exaggerated claims to demo kinetic ascendancy while attempting to project terror tactics against civilians to claim social space.",
        "FAH propagated exaggerated successes to maintain kinetic relevance while alleging state repression to legitimize external resistance campaigns."
      ]
    }
  }
  ```

---

### 2.5. Daily Operational Reports (DOR) & PowerPoint Briefs
1. **List Available PPTX Decks**:
   - **Endpoint**: `GET /api/deck/list`
   - **Response**:
     ```json
     {
       "decks": [
         {
           "filename": "daily_osint_20260904_1251.pptx",
           "size_bytes": 16604096,
           "size_formatted": "15.83 MB",
           "modified_at": "2026-09-04 12:51:05",
           "modified_timestamp": 1788508265.598
         }
       ]
     }
     ```

2. **Download PPTX Deck**:
   - **Endpoint**: `GET /api/deck/download?filename=<filename.pptx>`
   - **Response Headers**:
     - `Content-Type: application/vnd.openxmlformats-officedocument.presentationml.presentation`
     - `Content-Disposition: attachment; filename="<filename.pptx>"`
   - **Body**: Binary presentation data.

3. **Trigger Deck Generation Pipeline (Optional)**:
   - **Endpoint**: `POST /api/deck/generate`
   - **Body**:
     ```json
     {
       "hours": 24,
       "no_pull": false,
       "prev_day": false
     }
     ```
   - **Response**:
     ```json
     {
       "status": "triggered",
       "message": "Deck generation started in background"
     }
     ```

---

### 2.6. Intercepted Media & Image Server
- **Endpoint**: `GET /api/media?path=<sentiment_image_path>`
- **Behavior**:
  - Validates Basic Auth header.
  - Reads image or video from storage directory.
  - Returns raw binary stream with appropriate `Content-Type` (`image/png`, `image/jpeg`, `video/mp4`).
  - Cache-Control header: `public, max-age=86400`.

---

## 3. Recommended Python / FastAPI Blueprint
```python
from fastapi import FastAPI, Depends, HTTPException, Query, Security
from fastapi.security import HTTPBasic, HTTPBasicCredentials
from fastapi.responses import FileResponse
from pydantic import BaseModel
import secrets, os

app = FastAPI(title="Talkwalker OSINT Hub API")
security = HTTPBasic()

def auth_user(credentials: HTTPBasicCredentials = Depends(security)):
    is_user_ok = secrets.compare_digest(credentials.username, "CANTTHINKOFNTHIN")
    is_pass_ok = secrets.compare_digest(credentials.password, "NOTFASCIST")
    if not (is_user_ok and is_pass_ok):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return credentials.username

@app.post("/api/login")
def login(user: str = Depends(auth_user)):
    return {"status": "authenticated", "user": user}

@app.get("/api/stats")
def get_stats(hours: int = None, user: str = Depends(auth_user)):
    # Query database and return StatsResponse schema
    return { ... }

@app.get("/api/posts")
def get_posts(
    page: int = 1,
    limit: int = 25,
    category: str = None,
    attack_type: str = None,
    pipeline: str = None,
    hours: int = None,
    search: str = None,
    user: str = Depends(auth_user)
):
    # Query database with pagination and filters
    return { ... }

@app.get("/api/deck/list")
def list_decks(user: str = Depends(auth_user)):
    # Scan PPTX directory
    return { "decks": [ ... ] }

@app.get("/api/deck/download")
def download_deck(filename: str, user: str = Depends(auth_user)):
    file_path = os.path.join("/path/to/decks", filename)
    return FileResponse(file_path, filename=filename, media_type="application/vnd.openxmlformats-officedocument.presentationml.presentation")

@app.get("/api/media")
def get_media(path: str, user: str = Depends(auth_user)):
    return FileResponse(path)
```
