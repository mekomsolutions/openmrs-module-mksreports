## Vaccination
This report searches different vaccinations. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
        "report.vaccination.active": "true",
        "report.ecv.active": "true",
        "report.prenatalVisitType": "uuid-to-prenatal-visitType",
        "report.vaccinations": "uuid-to-vaccinations-question-concept",
        "report.vaccinationSequenceNumber": "uuid-to-vaccination-sequence-number-concept",
        "report.boosterSequenceNumber": "uuid-to-vaccination-booster-number-concept",
        "report.vaccinationList":"uuids-to-vaccinations:booster-or-sequence-number",
        "report.ecvList" : "uuid-to-ecv:booster-or-sequence-number"
}
```
`report.vaccination.active` activates the individual vaccinations report to be usable when the module is loaded.
`report.vaccination.active` activates the ECV part of report to be usable when the module is loaded.
`report.prenatalVisitType` specifies the uuid  of prenatal visit to filter on.
`report.vaccinations` specifies the uuid of vaccination question concept.
`report.vaccinationSequenceNumber`specifies uuid of the vaccination sequence number concept.
`report.boosterSequenceNumber` specifies the uuid of the vaccination booster number concept.
`report.vaccinationList` specifies the uuids of individual vaccinations with their sequence or booster number separated by a full-colon.
`report.ecvList` specifies the uuids of individual vaccinations for the full pediatric coverage with their sequence or booster number separated by a full-colon. Currently the report expect this to be all children that have received 1 dose of BCG, 1 dose of VPI, 3 doses of Penta, 2 doses of VPO, 2 doses of Rota and 1 dose of RR

The report template can be found at [MSPP: Vaccination](https://docs.google.com/spreadsheets/d/13A3gBRwi45-YwnArNsDgQB4EPVwsTswp/edit#gid=1856133398)