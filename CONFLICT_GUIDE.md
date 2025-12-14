# Merge Conflict Quick Guide

If you see the VS Code prompts shown in the screenshots ("Accept current change", "Accept incoming change", "Accept both changes"), use these choices for this project:

- **Driver creation block (`DriverServiceImpl.create`)** – choose **Accept current change** to keep the MapStruct-based mapping (`driverMapper.toEntity(request)`), then set `setIsActive(true)`. The incoming version duplicates manual field copies we no longer use.

- **Vehicle category creation block (`VehicleCategoryServiceImpl.create`)** – choose **Accept current change** so we keep the mapper-based construction. The incoming version is the older manual setter flow.

- **Vehicle creation block (`VehicleServiceImpl.create`)** – choose **Accept current change** to preserve the validated parsing helpers (`parseFuelType`, `parseFuelConsumption`, `parseMaintenanceIntervalWeeks`, etc.) and mapper-based entity creation.

- **`application.yaml` caching section** – choose **Accept current change** to retain the Redis cache configuration (`cache.type: redis` and TTL), then manually reinstate any missing settings below if needed. The incoming chunk was an earlier baseline without Redis cache wiring.

- **`db.changelog-master.xml` includes** – choose **Accept current change** so that all Liquibase changesets (categories, drivers, vehicles, movements, users/roles, indexes, seeds) remain registered in execution order. The incoming version omitted later includes.

If you end up with both versions in the file, prefer the mapper/Redis/Liquibase-enabled variant and delete the older manual setters so the code stays consistent with the current architecture.
