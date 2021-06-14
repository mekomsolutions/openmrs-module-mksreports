SELECT tab1.`Category` 'Categories',
                       tab4.`New visits` 'New_visits',
                                         tab4.`Subsequent visits` 'Subsequent_visits'
FROM
  (SELECT ':commonreports.report.visits.category1.label' AS 'Category'
   UNION ALL SELECT ':commonreports.report.visits.category2.label'
   UNION ALL SELECT ':commonreports.report.visits.category3.label'
   UNION ALL SELECT ':commonreports.report.visits.category4.label'
   UNION ALL SELECT ':commonreports.report.visits.category5.label'
   UNION ALL SELECT ':commonreports.report.visits.category6.label'
   UNION ALL SELECT ':commonreports.report.visits.category7.label'
   UNION ALL SELECT ':commonreports.report.visits.category8.label'
   UNION ALL SELECT ':commonreports.report.visits.category9.label'
   UNION ALL SELECT ':commonreports.report.visits.total.label') tab1
LEFT OUTER JOIN
  (SELECT COALESCE(tab3.`Category`, ':commonreports.report.visits.total.label') 'Category',
                                                  SUM(tab3.`New visits`) 'New visits',
                                                                                  SUM(tab3.`Subsequent visits`) 'Subsequent visits'
   FROM
     (SELECT tab2.`Category` 'Category',
                             nullif(sum(tab2.`New visits`), 0) 'New visits',
                                                               nullif(sum(tab2.`Subsequent visits`), 0) 'Subsequent visits'
      FROM
        (SELECT (CASE
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 1 THEN ':commonreports.report.visits.category1.label'
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 >= 1
                          AND DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 5 THEN ':commonreports.report.visits.category2.label'
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 >= 5
                          AND DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 10 THEN ':commonreports.report.visits.category3.label'
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 >= 10
                          AND DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 15 THEN ':commonreports.report.visits.category4.label'
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 >= 15
                          AND DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 20
                          AND v.visit_type_id <> :prenatalVisitTypeId
                          AND v.visit_type_id <> :familyPlanningVisitTypeId THEN ':commonreports.report.visits.category5.label'
                     WHEN DATEDIFF(v.date_started, pr.birthdate)/ 365.25 >= 20
                          AND DATEDIFF(v.date_started, pr.birthdate)/ 365.25 < 25
                          AND v.visit_type_id <> :prenatalVisitTypeId
                          AND v.visit_type_id <> :familyPlanningVisitTypeId THEN ':commonreports.report.visits.category6.label'
                     WHEN v.visit_type_id = :prenatalVisitTypeId THEN ':commonreports.report.visits.category7.label'
                     WHEN v.visit_type_id = :familyPlanningVisitTypeId THEN ':commonreports.report.visits.category8.label'
                     ELSE ':commonreports.report.visits.category9.label'
                 END) 'Category',
                      IF(IFNULL(prev_visit.visit_id, 'null') = 'null', '1', ' ') 'New visits',
                                                                                 IF(IFNULL(prev_visit.visit_id, 'null') = 'null', ' ', '1') 'Subsequent visits'
         FROM patient p 

         -- Person

         INNER JOIN person pr ON p.patient_id = pr.person_id
         AND pr.voided = 0 

         -- Visit

         INNER JOIN visit v ON p.patient_id = v.patient_id
         AND v.voided = 0 

         -- New or subsequent visit

         LEFT OUTER JOIN visit prev_visit ON prev_visit.patient_id = v.patient_id
         AND prev_visit.date_started < v.date_started
         WHERE p.voided = 0
           AND date(v.date_started) BETWEEN :startDate AND :endDate
         GROUP BY v.visit_id) tab2
      GROUP BY tab2.`Category`) tab3
   GROUP BY tab3.Category WITH ROLLUP) tab4 ON tab1.Category = tab4.Category;