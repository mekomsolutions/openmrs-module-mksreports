## Lab
This report searches different lab tests. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [Initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.lab.active" : "true",
    "report.lab.serialSputumBacilloscopy" : "uuid-to-serial-sputum-bacilloscopy-concept",
    "report.lab.positive" : "uuid-to-positive-concept",
    "report.lab.negative" : "uuid-to-negative-concept",
    "report.lab.indeterminate" : "uuid-to-indeterminate-concept",
    "report.lab.zero" : "uuid-to-zero-concept",
    "report.lab.onePlus" : "uuid-to-One-plus-concept",
    "report.lab.twoPlus" : "uuid-to-Two-plus-concept",
    "report.lab.threePlus" : "uuid-to-Three-plus-concept",
    "report.lab.fourPlus" : "uuid-to-Four-plus-concept",
    "report.lab.malaria" : "uuid-to-Malaria-concept",
    "report.lab.completeBloodCount" : "uuid-to-Complete-blood-count-concept",
    "report.lab.sicklingTest" : "uuid-to-Sickling-test-concept",
    "report.lab.bloodGroup" : "uuid-to-Blood-group-concept",
    "report.lab.urinalysis" : "uuid-to-Urinalysis-concept",
    "report.lab.prenatalVisitType" : "uuid-to-Prenatal-visit-type"
}
```
The prenatal visit type filter is used on some of the lab tests to differetiate between pregnant mothers and other female patients.
`report.lab.active` activates the report to be usable when the module is loaded.
The report template can be found at [MSPP: Examens de laboratoire](https://docs.google.com/spreadsheets/d/13A3gBRwi45-YwnArNsDgQB4EPVwsTswp/edit#gid=2062213411)
