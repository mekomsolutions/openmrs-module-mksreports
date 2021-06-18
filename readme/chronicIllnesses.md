## Chronic Illnesses Report
This report searches through a new diagnoses and existing active conditions. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.chronicIllnesses.active" : "true",
    "report.chronicIllnesses.conceptSet" : "uuid-to-the-concept-set-of-diagnoses",
    "report.chronicIllnesses.referredTo.concept" : "uuid-to-referredTo-concept"
}
```
`report.chronicIllnesses.active` activates the report to be usable when the module is loaded.

`report.chronicIllnesses.conceptSet` specifies the diagnoses to filter from. These appear as separate rows for each on the report. Diagnoses can be aggregated to appear as one through a concept set added to this list.

`report.chronicIllnesses.referredTo.concept` specifies a coded question concept which allows for determining referred outcome cases.