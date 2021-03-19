select maladies.name "Maladies/Symptomes",
HL1 "M<1",
FL1 "F<1",
HL4 "M_1-4",
FL4 "F_1-4",
HL9 "M_5-9",
FL9 "F_5-9",
HL14 "M_10-14",
FL14 "F_10-14",
HL24 "M_15-24",
FL24 "F_15-24",
HL49 "M_25-49",
FL49 "F_25-49",
HG50 "M>50",
FG50 "F>50",
HTotal "M_Total",
FTotal "F_Total"
from
(select 'qD' as name
UNION ALL
select 'BID'
UNION ALL
select 'TID'
UNION ALL
select 'QID') maladies
LEFT OUTER JOIN
(
select
diagnosis "name",    
nullif(sum(ML1),0) "HL1",
nullif(sum(FL1),0) "FL1",
nullif(sum(ML4),0) "HL4",
nullif(sum(FL4),0) "FL4",
nullif(sum(ML9),0) "HL9",
nullif(sum(FL9),0) "FL9",
nullif(sum(ML14),0) "HL14",
nullif(sum(FL14),0) "FL14",
nullif(sum(ML24),0) "HL24",
nullif(sum(FL24),0) "FL24",
nullif(sum(ML49),0) "HL49",
nullif(sum(FL49),0) "FL49",
nullif(sum(MG50),0) "HG50",
nullif(sum(FG50),0) "FG50",
nullif(sum(ML1)+sum(ML4)+sum(ML9)+ sum(ML14)+sum(ML24)+sum(ML49)+sum(MG50),0) "HTotal",
nullif(sum(FL1)+sum(FL4)+sum(FL9)+ sum(FL14)+sum(FL24)+sum(FL49)+sum(FG50),0) "FTotal"
from (
select
(CASE 
  when o.value_coded = 12 then 'qD'
  when o.value_coded = 13 then 'BID'
  when o.value_coded = 14 then 'TID'
  when o.value_coded = 15 then 'QID'
end) 'diagnosis',
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
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 50 and pr.gender = 'M' then 1 else 0 end) "MG50",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 50 and pr.gender = 'F' then 1 else 0 end) "FG50"
from obs o
INNER JOIN person pr on pr.person_id = o.person_id
where o.concept_id = 11
and o.voided = 0 

-- Adding date params
AND date(o.obs_datetime) BETWEEN :startDate AND :endDate

-- Only consider latest obs
and not exists
   (select 1 from obs o_prev 
    where (date(o_prev.obs_datetime) > date(o.obs_datetime) or (date(o_prev.obs_datetime) = date(o.obs_datetime) and o_prev.obs_id > o.obs_id))
    and o_prev.person_id = o.person_id
    and o_prev.concept_id = o.concept_id
    and o_prev.value_coded = o.value_coded
    )
) oo
where diagnosis is not null
group by name
) t on maladies.name = t.name
;