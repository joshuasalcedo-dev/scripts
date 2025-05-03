title: "Implement Optimistic Locking Mechanism for Concurrent Data Access"

body:
## Description
We need to implement a robust optimistic locking strategy across our application to handle concurrent data modifications more effectively. While we have implemented optimistic locking in the Script entity, we should extend this pattern to all relevant entities to prevent data inconsistency issues.

## Current Behavior
Currently, only the Script entity uses optimistic locking. This leaves our application vulnerable to lost updates and data inconsistencies when multiple users attempt to modify the same records simultaneously in other entities.

## Expected Behavior
All entities that can be modified concurrently should implement optimistic locking with:
- Properly annotated @Version fields
- Consistent exception handling for OptimisticLockException
- Clear UI feedback when optimistic lock exceptions occur
- Retry mechanisms where appropriate

## Implementation Tasks
- [ ] Audit all entity classes to identify candidates for optimistic locking
- [ ] Add @Version fields to all relevant entities
- [ ] Create a standardized OptimisticLockException handler in GlobalExceptionHandler
- [ ] Implement user-friendly error messages for conflict scenarios
- [ ] Add documentation explaining the optimistic locking strategy
- [ ] Create unit and integration tests for concurrent modification scenarios

## Technical Details
- Use long type for version fields with a DEFAULT 0 constraint
- Follow the naming convention "optlock" for the version column
- Ensure proper handling of OptimisticLockException at service layer
- Consider implementing JPA EntityListeners for centralized version handling logic

## Business Value
This implementation will:
- Prevent data corruption due to concurrent modifications
- Improve data integrity across the application
- Provide better user experience by clearly communicating when conflicts occur
- Reduce support tickets related to unexplained data changes

## Priority
Medium-High: This should be addressed in the next sprint as it impacts data integrity.

labels: enhancement, data-integrity, concurrent-access
assignees: joshuasalcedo-dev