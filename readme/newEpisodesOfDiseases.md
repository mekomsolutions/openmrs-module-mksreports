## New Episodes of Diseases
This report searches through a mix of chief complaints and diagnoses. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...
    "report.newEpisodesOfDiseases.diseasesList.conceptSet" : "uuid-to-disease-list-concept-set",
    "report.newEpisodesOfDiseases.questions.conceptSet" : "uuid-to-concept-set-containing-questions",
    "report.newEpisodesOfDiseases.active" : "true"
}
```