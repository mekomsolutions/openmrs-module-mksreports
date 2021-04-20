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
    "report.newEpisodesOfDiseases.allDiagnoses.conceptSet" : "uuid-to-concept-set-containing-allDiagnoses"
}
```
`report.newEpisodesOfDiseases.active` activates the report to be usable when the module is loaded.

`report.newEpisodesOfDiseases.diagnosisList.conceptSet` specifies the diagnoses and/or chief complaints to filter from. These appear as separate rows for each on the report.

`report.newEpisodesOfDiseases.questions.conceptSet` specifies a concept set to the question concepts for which the recorded answer observations are being reported on.

`report.newEpisodesOfDiseases.allDiseases.conceptSet` specifies a concept set containing all diseases, from which those diseases not specified in the disease list are aggregated in the report as "All Other Diagnoses" on one row.

**Note:** Only latest observations are evaluated on a particular disease or chief-complaint on any patients. For example, if more than one observations on say  _Cholora_  was ever recorded on a patient, and  _Cholera_  is among the diseases list, then only the most recent observation is considered in the report.