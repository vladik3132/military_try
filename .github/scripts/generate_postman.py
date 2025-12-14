#!/usr/bin/env python3
"""
Generate variant-specific Postman collection for PZ 1/2 automated testing
Supports variants A, B, C with complete CRUD and validation tests
"""
import json
import sys


def generate_collection(variant):
    """Generate Postman collection for specific variant"""
    
    # Base collection structure
    collection = {
        "info": {
            "name": f"PZ 1/2 - Variant {variant} Tests",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8080"}
        ],
        "item": []
    }
    
    # Variant-specific configuration
    configs = {
        "A": {
            "category_endpoint": "supply-categories",
            "item_endpoint": "supply-items",
            "location_endpoint": "warehouses",
            "category_data": {
                "name": "Боєприпаси 5.45мм",
                "code": "AMMO-545",
                "description": "Test category",
                "requiresColdStorage": False
            },
            "location_data": {
                "name": "Центральний склад №1",
                "code": "WH-01",
                "address": "Test address",
                "capacity": 1000,
                "currentOccupancy": 50,
                "hasRefrigeration": False
            },
            "item_data": {
                "name": "Патрони 5.45х39",
                "batchNumber": "BATCH-TEST-001",
                "categoryId": "{{categoryId}}",
                "quantity": 5000,
                "unit": "шт",
                "expirationDate": "2029-12-31",
                "hazardClass": "EXPLOSIVE",
                "storageConditions": "Dry place",
                "warehouseId": "{{locationId}}"
            },
            "item_update": {
                "quantity": 4500,
                "storageConditions": "Updated conditions"
            },
            "unique_field": "batchNumber"
        },
        "B": {
            "category_endpoint": "vehicle-categories",
            "item_endpoint": "vehicles",
            "location_endpoint": "drivers",
            "category_data": {
                "name": "Вантажна техніка",
                "code": "TRUCK",
                "description": "Test category",
                "requiredLicense": "CE",
                "maxLoadCapacity": 10000
            },
            "location_data": {
                "militaryId": "UA12345678",
                "firstName": "Іван",
                "lastName": "Іваненко",
                "middleName": "Іванович",
                "rank": "Сержант",
                "licenseNumber": "AXX123456",
                "licenseCategories": "B,C,CE",
                "licenseExpiryDate": "2029-12-31",
                "phoneNumber": "+380501234567",
                "isActive": True
            },
            "item_data": {
                "model": "КрАЗ-6322",
                "registrationNumber": "AA1234BB",
                "categoryId": "{{categoryId}}",
                "engineNumber": "ENG123456",
                "chassisNumber": "CHAS123456",
                "manufactureYear": 2020,
                "mileage": 50000,
                "fuelType": "DIESEL",
                "fuelConsumption": 28.5,
                "maintenanceIntervalKm": 10000,
                "lastMaintenanceDate": "2024-01-15",
                "lastMaintenanceMileage": 45000,
                "driverId": "{{locationId}}",
                "status": "OPERATIONAL"
            },
            "item_update": {
                "mileage": 51000,
                "status": "IN_MAINTENANCE"
            },
            "unique_field": "registrationNumber"
        },
        "C": {
            "category_endpoint": "unit-types",
            "item_endpoint": "personnel",
            "location_endpoint": "military-units",
            "category_data": {
                "name": "Рота",
                "code": "COMPANY",
                "description": "Test unit type",
                "hierarchy": 3,
                "typicalSize": 100
            },
            "location_data": {
                "name": "1-а механізована рота",
                "code": "MECH-01",
                "unitTypeId": "{{categoryId}}",
                "location": "Test location",
                "formationDate": "2020-01-01",
                "strength": 100,
                "currentStrength": 95
            },
            "item_data": {
                "militaryId": "UA12345678",
                "firstName": "Олександр",
                "lastName": "Петренко",
                "middleName": "Васильович",
                "rank": "LIEUTENANT",
                "specialization": "Командир взводу",
                "unitId": "{{locationId}}",
                "contractStartDate": "2023-01-01",
                "contractEndDate": "2026-01-01",
                "securityClearance": "SECRET",
                "medicalCategory": "A1",
                "phoneNumber": "+380501234567",
                "email": "test@example.com"
            },
            "item_update": {
                "rank": "SENIOR_LIEUTENANT",
                "phoneNumber": "+380509999999"
            },
            "unique_field": "militaryId"
        }
    }
    
    if variant not in configs:
        print(f"Unknown variant: {variant}")
        sys.exit(1)
    
    config = configs[variant]
    
    # Test 1: Create Category
    collection['item'].append({
        "name": f"Create {config['category_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(config['category_data'], ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['category_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['category_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 201', function() {",
                    "  pm.response.to.have.status(201);",
                    "});",
                    "pm.test('Response has id', function() {",
                    "  pm.expect(pm.response.json()).to.have.property('id');",
                    "  pm.environment.set('categoryId', pm.response.json().id);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 2: Create Location/Driver/Unit
    collection['item'].append({
        "name": f"Create {config['location_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(config['location_data'], ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['location_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['location_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 201', function() {",
                    "  pm.response.to.have.status(201);",
                    "});",
                    "pm.test('Response has id', function() {",
                    "  pm.expect(pm.response.json()).to.have.property('id');",
                    "  pm.environment.set('locationId', pm.response.json().id);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 3: Create Item
    collection['item'].append({
        "name": f"Create {config['item_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(config['item_data'], ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 201', function() {",
                    "  pm.response.to.have.status(201);",
                    "});",
                    "pm.test('Response has all required fields', function() {",
                    "  var json = pm.response.json();",
                    "  pm.expect(json).to.have.property('id');",
                    f"  pm.expect(json).to.have.property('{config['unique_field']}');",
                    "  pm.expect(json).to.have.property('category');",
                    "  pm.expect(json.category).to.have.property('id');",
                    "  pm.environment.set('itemId', json.id);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 4: Get All Items
    collection['item'].append({
        "name": f"Get All {config['item_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "GET",
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 200', function() {",
                    "  pm.response.to.have.status(200);",
                    "});",
                    "pm.test('Response is array', function() {",
                    "  pm.expect(pm.response.json()).to.be.an('array');",
                    "  pm.expect(pm.response.json().length).to.be.greaterThan(0);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 5: Get Item by ID
    collection['item'].append({
        "name": f"Get {config['item_endpoint'].title().replace('-', ' ')} by ID",
        "request": {
            "method": "GET",
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}/{{{{itemId}}}}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint'], "{{itemId}}"]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 200', function() {",
                    "  pm.response.to.have.status(200);",
                    "});",
                    "pm.test('Item ID matches', function() {",
                    "  pm.expect(pm.response.json().id).to.equal(parseInt(pm.environment.get('itemId')));",
                    "});"
                ]
            }
        }]
    })
    
    # Test 6: Update Item
    collection['item'].append({
        "name": f"Update {config['item_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "PUT",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(config['item_update'], ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}/{{{{itemId}}}}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint'], "{{itemId}}"]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 200', function() {",
                    "  pm.response.to.have.status(200);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 7: Validation Error - Empty Required Fields
    invalid_item = config['item_data'].copy()
    invalid_item['name'] = ""
    invalid_item[config['unique_field']] = ""
    
    collection['item'].append({
        "name": "Validation Error - Empty Required Fields",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(invalid_item, ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 400', function() {",
                    "  pm.response.to.have.status(400);",
                    "});",
                    "pm.test('Error response has validation errors', function() {",
                    "  pm.expect(pm.response.json()).to.have.property('errors');",
                    "});"
                ]
            }
        }]
    })
    
    # Test 8: Duplicate Resource
    duplicate_item = config['item_data'].copy()
    duplicate_item['name'] = "Duplicate item"
    
    collection['item'].append({
        "name": f"Duplicate Resource - Same {config['unique_field']}",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(duplicate_item, ensure_ascii=False)
            },
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint']]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 409', function() {",
                    "  pm.response.to.have.status(409);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 9: Not Found - Non-existent ID
    collection['item'].append({
        "name": "Not Found - Non-existent ID",
        "request": {
            "method": "GET",
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}/99999",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint'], "99999"]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 404', function() {",
                    "  pm.response.to.have.status(404);",
                    "});"
                ]
            }
        }]
    })
    
    # Test 10: Delete Item
    collection['item'].append({
        "name": f"Delete {config['item_endpoint'].title().replace('-', ' ')}",
        "request": {
            "method": "DELETE",
            "url": {
                "raw": f"{{{{baseUrl}}}}/api/{config['item_endpoint']}/{{{{itemId}}}}",
                "host": ["{{baseUrl}}"],
                "path": ["api", config['item_endpoint'], "{{itemId}}"]
            }
        },
        "event": [{
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status code is 204', function() {",
                    "  pm.response.to.have.status(204);",
                    "});"
                ]
            }
        }]
    })
    
    return collection


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python generate_postman.py <variant>")
        print("Variants: A, B, C")
        sys.exit(1)
    
    variant = sys.argv[1].upper()
    collection = generate_collection(variant)
    
    with open('postman-collection.json', 'w', encoding='utf-8') as f:
        json.dump(collection, f, indent=2, ensure_ascii=False)
    
    print(f"✅ Generated Postman collection for variant {variant}")
    print(f"📄 Output: postman-collection.json")
    print(f"📊 Tests: {len(collection['item'])}")
