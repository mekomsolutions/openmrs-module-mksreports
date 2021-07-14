select maladies.name "Maladies/Symptomes",
MalesL1 "M<1",
FemalesL1 "F<1",
MalesL4 "M_1-4",
FemalesL4 "F_1-4",
MalesL9 "M_5-9",
FemalesL9 "F_5-9",
MalesL14 "M_10-14",
FemalesL14 "F_10-14",
MalesL24 "M_15-24",
FemalesL24 "F_15-24",
MalesL49 "M_25-49",
FemalesL49 "F_25-49",
MalesG50 "M>50",
FemalesG50 "F>50",
MalesTotal "M_Total",
FemalesTotal "F_Total",
TotalReferredCases
from
(:selectStatements) maladies
LEFT OUTER JOIN
(
select
diagnosis "name",    
nullif(sum(ML1),0) "MalesL1",
nullif(sum(FL1),0) "FemalesL1",
nullif(sum(ML4),0) "MalesL4",
nullif(sum(FL4),0) "FemalesL4",
nullif(sum(ML9),0) "MalesL9",
nullif(sum(FL9),0) "FemalesL9",
nullif(sum(ML14),0) "MalesL14",
nullif(sum(FL14),0) "FemalesL14",
nullif(sum(ML24),0) "MalesL24",
nullif(sum(FL24),0) "FemalesL24",
nullif(sum(ML49),0) "MalesL49",
nullif(sum(FL49),0) "FemalesL49",
nullif(sum(MG50),0) "MalesG50",
nullif(sum(FG50),0) "FemalesG50",
nullif(sum(ML1)+sum(ML4)+sum(ML9)+ sum(ML14)+sum(ML24)+sum(ML49)+sum(MG50),0) "MalesTotal",
nullif(sum(FL1)+sum(FL4)+sum(FL9)+ sum(FL14)+sum(FL24)+sum(FL49)+sum(FG50),0) "FemalesTotal",
nullif(sum(TotalRefCases),0) "TotalReferredCases"
from (
select
(CASE 
  :whenStatements
end) "diagnosis",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 1 and pr.gender = 'M' then 1 else 0 end) "ML1",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 1 and pr.gender = 'F' then 1 else 0 end) "FL1",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 1 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <5 and pr.gender = 'M' then 1 else 0 end) "ML4",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 1 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <5 and pr.gender = 'F' then 1 else 0 end) "FL4",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <10 and pr.gender = 'M' then 1 else 0 end) "ML9",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <10 and pr.gender = 'F' then 1 else 0 end) "FL9",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 10 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'M' then 1 else 0 end) "ML14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 10 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'F' then 1 else 0 end) "FL14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <25 and pr.gender = 'M' then 1 else 0 end) "ML24",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <25 and pr.gender = 'F' then 1 else 0 end) "FL24",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 25 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'M' then 1 else 0 end) "ML49",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 25 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'F' then 1 else 0 end) "FL49",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 50 and pr.gender = 'M' then 1 else 0 end) "MG50",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 50 and pr.gender = 'F' then 1 else 0 end) "FG50",
(CASE when o_referred.obs_id IS NOT NULL then 1 else 0 end) "TotalRefCases"
from obs o
INNER JOIN person pr on pr.person_id = o.person_id

-- Adding referred cases
LEFT OUTER JOIN obs o_referred on o_referred.person_id = o.person_id and o_referred.voided = 0 and o_referred.concept_id = :referralConcept and (select visit_id from encounter e1 where e1.encounter_id = o_referred.encounter_id) = (select visit_id from encounter e2 where e2.encounter_id = o.encounter_id) 
where o.concept_id in (:conceptIds)
and o.voided = 0 

-- Adding date params
AND date(o.obs_datetime) BETWEEN :startDate AND :endDate

) oo
where diagnosis is not null
group by name
) t on maladies.name = t.name
;