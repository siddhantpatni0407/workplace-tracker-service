# User Notes API Documentation

## Overview
The User Notes API provides comprehensive CRUD operations, filtering, search, statistics, and bulk operations for user notes management in the workplace tracker service.

## Base URL
```
http://localhost:8010/api/v1/workplace-tracker-service
```

## Authentication
All endpoints require JWT Bearer token authentication with USER role.

---

## API Endpoints

### 1. Create Note
**Endpoint:** `POST /notes`  
**Description:** Create a new note for the authenticated user  
**Authentication:** Required (USER role)

**Request Body:**
```json
{
    "noteTitle": "My Important Note",
    "noteContent": "This is the content of my note",
    "noteType": "TEXT",
    "color": "YELLOW",
    "category": "WORK",
    "priority": "HIGH",
    "status": "ACTIVE",
    "isPinned": false,
    "isShared": false,
    "reminderDate": "2025-12-31 10:00:00"
}
```

**Response (201 Created):**
```json
{
    "status": "SUCCESS",
    "message": "Note created successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "My Important Note",
        "noteContent": "This is the content of my note",
        "noteType": "TEXT",
        "color": "YELLOW",
        "category": "WORK",
        "priority": "HIGH",
        "status": "ACTIVE",
        "isPinned": false,
        "isShared": false,
        "reminderDate": "2025-12-31 10:00:00",
        "version": 1,
        "accessCount": 0,
        "lastAccessedDate": null,
        "createdDate": "2025-10-12 10:30:00",
        "modifiedDate": "2025-10-12 10:30:00"
    }
}
```

---

### 2. Get Note by ID
**Endpoint:** `GET /notes`  
**Description:** Get a specific note by ID for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to retrieve

**Example:** `GET /notes?noteId=1`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note retrieved successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "My Important Note",
        "noteContent": "This is the content of my note",
        "noteType": "TEXT",
        "color": "YELLOW",
        "category": "WORK",
        "priority": "HIGH",
        "status": "ACTIVE",
        "isPinned": false,
        "isShared": false,
        "reminderDate": "2025-12-31 10:00:00",
        "version": 1,
        "accessCount": 1,
        "lastAccessedDate": "2025-10-12 11:00:00",
        "createdDate": "2025-10-12 10:30:00",
        "modifiedDate": "2025-10-12 10:30:00"
    }
}
```

---

### 3. Get All User Notes (with Filters)
**Endpoint:** `GET /notes/user`  
**Description:** Get all notes for the authenticated user with pagination and optional filters  
**Authentication:** Required (USER role)

**Query Parameters:**
- `page` (optional, default: 0): Page number for pagination
- `limit` (optional, default: 20): Number of items per page
- `noteType` (optional): Filter by note type (TEXT, CHECKLIST, VOICE, IMAGE, LINK)
- `color` (optional): Filter by color (DEFAULT, RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, PINK)
- `category` (optional): Filter by category (PERSONAL, WORK, STUDY, HEALTH, FINANCE, TRAVEL, SHOPPING, OTHER)
- `priority` (optional): Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `status` (optional): Filter by status (ACTIVE, ARCHIVED, DELETED)
- `isPinned` (optional): Filter by pinned status (true/false)
- `isShared` (optional): Filter by shared status (true/false)
- `searchTerm` (optional): Search term for title and content
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order (asc/desc)
- `startDate` (optional): Filter by created date range start (ISO format)
- `endDate` (optional): Filter by created date range end (ISO format)

**Example:** `GET /notes/user?page=0&limit=10&noteType=TEXT&color=YELLOW&category=WORK&priority=HIGH&isPinned=true&sortBy=createdDate&sortOrder=desc`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Notes retrieved successfully",
    "data": {
        "data": [
            {
                "userNoteId": 1,
                "userId": 123,
                "noteTitle": "My Important Note",
                "noteContent": "This is the content of my note",
                "noteType": "TEXT",
                "color": "YELLOW",
                "category": "WORK",
                "priority": "HIGH",
                "status": "ACTIVE",
                "isPinned": true,
                "isShared": false,
                "reminderDate": "2025-12-31 10:00:00",
                "version": 1,
                "accessCount": 5,
                "lastAccessedDate": "2025-10-12 11:00:00",
                "createdDate": "2025-10-12 10:30:00",
                "modifiedDate": "2025-10-12 10:30:00"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 0,
        "pageSize": 10,
        "hasNext": false,
        "hasPrevious": false
    }
}
```

---

### 4. Update Note
**Endpoint:** `PUT /notes`  
**Description:** Update an existing note for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to update

**Request Body:**
```json
{
    "noteTitle": "Updated Note Title",
    "noteContent": "Updated content of my note",
    "noteType": "TEXT",
    "color": "BLUE",
    "category": "PERSONAL",
    "priority": "MEDIUM",
    "status": "ACTIVE",
    "isPinned": true,
    "isShared": false,
    "reminderDate": "2025-12-31 15:00:00"
}
```

**Example:** `PUT /notes?noteId=1`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note updated successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "Updated Note Title",
        "noteContent": "Updated content of my note",
        "noteType": "TEXT",
        "color": "BLUE",
        "category": "PERSONAL",
        "priority": "MEDIUM",
        "status": "ACTIVE",
        "isPinned": true,
        "isShared": false,
        "reminderDate": "2025-12-31 15:00:00",
        "version": 2,
        "accessCount": 5,
        "lastAccessedDate": "2025-10-12 11:00:00",
        "createdDate": "2025-10-12 10:30:00",
        "modifiedDate": "2025-10-12 12:00:00"
    }
}
```

---

### 5. Delete Note
**Endpoint:** `DELETE /notes`  
**Description:** Delete a note for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to delete
- `permanent` (optional, default: false): Whether to permanently delete the note

**Example:** `DELETE /notes?noteId=1&permanent=false`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note deleted successfully",
    "data": null
}
```

---

### 6. Get Notes by Type
**Endpoint:** `GET /notes/by-type`  
**Description:** Get notes by specific type for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteType` (required): The type of notes to retrieve (TEXT, CHECKLIST, VOICE, IMAGE, LINK)
- `page` (optional, default: 0): Page number
- `limit` (optional, default: 20): Page size
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order

**Example:** `GET /notes/by-type?noteType=TEXT&page=0&limit=10&sortBy=createdDate&sortOrder=desc`

---

### 7. Get Notes by Category
**Endpoint:** `GET /notes/by-category`  
**Description:** Get notes by specific category for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `category` (required): The category of notes to retrieve (PERSONAL, WORK, STUDY, HEALTH, FINANCE, TRAVEL, SHOPPING, OTHER)
- `page` (optional, default: 0): Page number
- `limit` (optional, default: 20): Page size
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order

**Example:** `GET /notes/by-category?category=WORK&page=0&limit=10&sortBy=priority&sortOrder=desc`

---

### 8. Get Pinned Notes
**Endpoint:** `GET /notes/pinned`  
**Description:** Get all pinned notes for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `page` (optional, default: 0): Page number
- `limit` (optional, default: 20): Page size
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order

**Example:** `GET /notes/pinned?page=0&limit=10&sortBy=createdDate&sortOrder=desc`

---

### 9. Get Archived Notes
**Endpoint:** `GET /notes/archived`  
**Description:** Get all archived notes for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `page` (optional, default: 0): Page number
- `limit` (optional, default: 20): Page size
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order

**Example:** `GET /notes/archived?page=0&limit=10&sortBy=modifiedDate&sortOrder=desc`

---

### 10. Search Notes
**Endpoint:** `GET /notes/search`  
**Description:** Search notes by title and content for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `query` (required): Search query string
- `page` (optional, default: 0): Page number
- `limit` (optional, default: 20): Page size
- `sortBy` (optional, default: modifiedDate): Sort field
- `sortOrder` (optional, default: desc): Sort order

**Example:** `GET /notes/search?query=important&page=0&limit=10&sortBy=createdDate&sortOrder=desc`

---

### 11. Update Note Status
**Endpoint:** `PATCH /notes/status`  
**Description:** Update note status for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to update
- `status` (required): The new status (ACTIVE, ARCHIVED, DELETED)

**Example:** `PATCH /notes/status?noteId=1&status=ARCHIVED`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note status updated successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "My Important Note",
        "noteContent": "This is the content of my note",
        "status": "ARCHIVED",
        "modifiedDate": "2025-10-12 12:30:00"
    }
}
```

---

### 12. Toggle Pin Status
**Endpoint:** `PATCH /notes/pin`  
**Description:** Toggle pin status for a note for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to toggle pin status

**Example:** `PATCH /notes/pin?noteId=1`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note pin status toggled successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "My Important Note",
        "noteContent": "This is the content of my note",
        "isPinned": true,
        "modifiedDate": "2025-10-12 12:45:00"
    }
}
```

---

### 13. Update Note Color
**Endpoint:** `PATCH /notes/color`  
**Description:** Update note color for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to update
- `color` (required): The new color (DEFAULT, RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, PINK)

**Example:** `PATCH /notes/color?noteId=1&color=GREEN`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note color updated successfully",
    "data": {
        "userNoteId": 1,
        "userId": 123,
        "noteTitle": "My Important Note",
        "noteContent": "This is the content of my note",
        "color": "GREEN",
        "modifiedDate": "2025-10-12 13:00:00"
    }
}
```

---

### 14. Get Note Statistics
**Endpoint:** `GET /notes/stats`  
**Description:** Get note statistics for the authenticated user  
**Authentication:** Required (USER role)

**Example:** `GET /notes/stats`

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Note statistics retrieved successfully",
    "data": {
        "totalNotes": 25,
        "activeNotes": 20,
        "archivedNotes": 4,
        "deletedNotes": 1,
        "pinnedNotes": 5,
        "sharedNotes": 3,
        "notesByType": {
            "TEXT": 20,
            "CHECKLIST": 3,
            "VOICE": 1,
            "IMAGE": 1,
            "LINK": 0
        },
        "notesByCategory": {
            "WORK": 15,
            "PERSONAL": 8,
            "STUDY": 2,
            "HEALTH": 0,
            "FINANCE": 0,
            "TRAVEL": 0,
            "SHOPPING": 0,
            "OTHER": 0
        },
        "notesByPriority": {
            "LOW": 5,
            "MEDIUM": 12,
            "HIGH": 7,
            "URGENT": 1
        }
    }
}
```

---

### 15. Bulk Update Notes
**Endpoint:** `PUT /notes/bulk-update`  
**Description:** Bulk update multiple notes for the authenticated user  
**Authentication:** Required (USER role)

**Request Body:**
```json
{
    "noteIds": [1, 2, 3],
    "updates": {
        "category": "WORK",
        "priority": "HIGH",
        "color": "YELLOW",
        "status": "ACTIVE"
    }
}
```

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Notes bulk updated successfully",
    "data": [
        {
            "userNoteId": 1,
            "userId": 123,
            "noteTitle": "Note 1",
            "category": "WORK",
            "priority": "HIGH",
            "color": "YELLOW",
            "status": "ACTIVE"
        },
        {
            "userNoteId": 2,
            "userId": 123,
            "noteTitle": "Note 2",
            "category": "WORK",
            "priority": "HIGH",
            "color": "YELLOW",
            "status": "ACTIVE"
        }
    ]
}
```

---

### 16. Bulk Delete Notes
**Endpoint:** `DELETE /notes/bulk-delete`  
**Description:** Bulk delete multiple notes for the authenticated user  
**Authentication:** Required (USER role)

**Request Body:**
```json
{
    "noteIds": [1, 2, 3],
    "permanentDelete": false
}
```

**Response (200 OK):**
```json
{
    "status": "SUCCESS",
    "message": "Notes bulk deleted successfully",
    "data": null
}
```

---

### 17. Duplicate Note
**Endpoint:** `POST /notes/duplicate`  
**Description:** Duplicate a note for the authenticated user  
**Authentication:** Required (USER role)

**Query Parameters:**
- `noteId` (required): The ID of the note to duplicate

**Example:** `POST /notes/duplicate?noteId=1`

**Response (201 Created):**
```json
{
    "status": "SUCCESS",
    "message": "Note duplicated successfully",
    "data": {
        "userNoteId": 26,
        "userId": 123,
        "noteTitle": "Copy of My Important Note",
        "noteContent": "This is the content of my note",
        "noteType": "TEXT",
        "color": "YELLOW",
        "category": "WORK",
        "priority": "HIGH",
        "status": "ACTIVE",
        "isPinned": false,
        "isShared": false,
        "reminderDate": null,
        "version": 1,
        "accessCount": 0,
        "lastAccessedDate": null,
        "createdDate": "2025-10-12 14:00:00",
        "modifiedDate": "2025-10-12 14:00:00"
    }
}
```

---

## Data Models

### UserNotesDTO Structure
```json
{
    "userNoteId": "Long - Unique identifier for the note",
    "userId": "Long - ID of the user who owns the note",
    "noteTitle": "String - Title of the note (max 500 characters, required)",
    "noteContent": "String - Content of the note (required)",
    "noteType": "Enum - Type of note (TEXT, CHECKLIST, VOICE, IMAGE, LINK)",
    "color": "Enum - Color of the note (DEFAULT, RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, PINK)",
    "category": "Enum - Category of the note (PERSONAL, WORK, STUDY, HEALTH, FINANCE, TRAVEL, SHOPPING, OTHER)",
    "priority": "Enum - Priority level (LOW, MEDIUM, HIGH, URGENT)",
    "status": "Enum - Status of the note (ACTIVE, ARCHIVED, DELETED)",
    "isPinned": "Boolean - Whether the note is pinned",
    "isShared": "Boolean - Whether the note is shared",
    "reminderDate": "DateTime - Reminder date and time (ISO format)",
    "version": "Integer - Version number for optimistic locking",
    "accessCount": "Integer - Number of times the note was accessed",
    "lastAccessedDate": "DateTime - Last access date and time",
    "createdDate": "DateTime - Creation date and time",
    "modifiedDate": "DateTime - Last modification date and time"
}
```

### Enums

#### NoteType
- `TEXT` - Plain text note
- `CHECKLIST` - Checklist note
- `VOICE` - Voice note
- `IMAGE` - Image note
- `LINK` - Link/URL note

#### NoteColor
- `DEFAULT` - Default color
- `RED` - Red color
- `ORANGE` - Orange color
- `YELLOW` - Yellow color
- `GREEN` - Green color
- `BLUE` - Blue color
- `PURPLE` - Purple color
- `PINK` - Pink color

#### NoteCategory
- `PERSONAL` - Personal notes
- `WORK` - Work-related notes
- `STUDY` - Study/educational notes
- `HEALTH` - Health-related notes
- `FINANCE` - Financial notes
- `TRAVEL` - Travel notes
- `SHOPPING` - Shopping lists/notes
- `OTHER` - Other categories

#### NotePriority
- `LOW` - Low priority
- `MEDIUM` - Medium priority
- `HIGH` - High priority
- `URGENT` - Urgent priority

#### NoteStatus
- `ACTIVE` - Active note
- `ARCHIVED` - Archived note
- `DELETED` - Deleted note (soft delete)

---

## Error Responses

### Common Error Codes
- `400 Bad Request` - Invalid request parameters or missing required fields
- `401 Unauthorized` - Authentication required or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Note not found or no notes found matching criteria
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
    "status": "FAILED",
    "message": "Error description",
    "data": null
}
```

---

## Usage Examples

### Creating a Simple Text Note
```bash
curl -X POST "http://localhost:8010/api/v1/workplace-tracker-service/notes" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Meeting Notes",
    "noteContent": "Discussed project timeline and deliverables",
    "noteType": "TEXT",
    "category": "WORK",
    "priority": "HIGH"
  }'
```

### Searching Notes
```bash
curl -X GET "http://localhost:8010/api/v1/workplace-tracker-service/notes/search?query=meeting&page=0&limit=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Getting User's Work Notes
```bash
curl -X GET "http://localhost:8010/api/v1/workplace-tracker-service/notes/user?category=WORK&priority=HIGH&page=0&limit=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Notes
- All date fields use ISO 8601 format: `YYYY-MM-DD HH:MM:SS`
- Pagination starts from page 0
- Soft delete is used by default (permanent=false)
- All endpoints require valid JWT authentication
- Maximum note title length is 500 characters
- Note content supports LONGTEXT (MySQL) for large content
