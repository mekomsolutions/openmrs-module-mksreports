select e.encounter_id, e.uuid
from encounter e
where e.encounter_id in (:encounterIds)