## New Episodes of Diseases
This report searches through a mix of chief complaints and diagnoses. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.newEpisodesOfDiseases.active" : "true",
    "report.newEpisodesOfDiseases.diagnosisList.conceptSet" : "uuid-to-diagnosis-list-concept-set",
    "report.newEpisodesOfDiseases.questions.conceptSet" : "uuid-to-concept-set-containing-questions",
    "report.newEpisodesOfDiseases.allDiagnoses.conceptSet" : "uuid-to-concept-set-containing-allDiagnoses",
    "report.newEpisodesOfDiseases.referredToConcept.concept" : "uuid-for-referred-to-healthCenter-questionConcept"
}
```
`report.newEpisodesOfDiseases.active` activates the report to be usable when the module is loaded.

`report.newEpisodesOfDiseases.diagnosisList.conceptSet` specifies the diagnoses and/or chief complaints to filter from. These appear as separate rows for each on the report.

`report.newEpisodesOfDiseases.questions.conceptSet` specifies a concept set to the question concepts for which the recorded answer observations are being reported on.

`report.newEpisodesOfDiseases.allDiseases.conceptSet` specifies a concept set containing all diseases, from which those diseases not specified in the disease list are aggregated in the report as "All Other Diagnoses" on one row. Else it can be a super set of sets of diagnoses, meaning it could contain sets where each has member diagnoses.

`report.newEpisodesOfDiseases.referredToConcept.concept` specifies a concept for reporting on referred patients. It could be a coded question which when answered means the patient has been referred to a different health center.

Find the report template at [MSPP: New Episodes of Diseases](https://docs.google.com/spreadsheets/d/13A3gBRwi45-YwnArNsDgQB4EPVwsTswp/edit#gid=704979704)