## Visits Report
This report searches through patient visits, determining whether fall in the 'New' or 'Follow-up' category. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.visits.active" : "true",
    "report.visits.prenatal.visitType.uuid" : "35ba9aff-901c-49dc-8630-a59385480d18",
    "report.visits.familyPlanning.visitType.uuid" : "c4643116-8a61-499f-b62b-ff9375db0b7d"
}
```
`report.visits.active` activates the report to be usable when the module is loaded.

`report.visits.prenatal.visitType.uuid` specifies the prenatal visit type.

`report.visits.familyPlanning.visitType.uuid` specifies the family planning visit type.

**Note**
'Prenatal' and 'Family planning' visits are reported on separately and therefore have to be specified in the above properties.

Find the report template at [MSPP Statistics Report - Visits](https://docs.google.com/spreadsheets/d/13A3gBRwi45-YwnArNsDgQB4EPVwsTswp/edit#gid=1723158101).