select o.obs_id, c.name
from obs o
left outer join concept_name c
  on o.concept_id = c.concept_id
  and c.voided = 0
where o.obs_id in (:obsIds)