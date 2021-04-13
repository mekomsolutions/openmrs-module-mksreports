## New Episodes of Diseases
This report searches through a mix of chief complaints and diagnoses. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.newEpisodesOfDiseases.active" : "true",
    "report.newEpisodesOfDiseases.diseasesList.conceptSet" : "uuid-to-disease-list-concept-set",
    "report.newEpisodesOfDiseases.questions.conceptSet" : "uuid-to-concept-set-containing-questions"
}
```
`report.newEpisodesOfDiseases.active` activates the report to be usable when the module is loaded.

`report.newEpisodesOfDiseases.diseasesList.conceptSet` specifies the diseases and/or chief complaints to filter from.

`report.newEpisodesOfDiseases.questions.conceptSet` specifies a concept set to the question concepts for which the recorded answer observations are being reported on.

**Note:** Only latest observations are evaluated on a particular disease or chief-complaint on any patients. For example, if more than one observations on say  _Cholora_  was ever recorded on a patient, and  _Cholera_  is among the diseases list, then only the most recent observation is considered in the report.