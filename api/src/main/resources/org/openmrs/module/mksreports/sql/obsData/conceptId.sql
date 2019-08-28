select o.obs_id, o.concept_id
from obs o
where o.obs_id in (:obsIds)