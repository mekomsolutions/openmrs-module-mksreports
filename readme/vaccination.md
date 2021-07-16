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
        "report.vaccinationSequenceNumber": "uuid-to-vaccination-sequence-number",
        "report.boosterSequenceNumber": "uuid-to-vaccination-booster-number",
        "report.vaccinationList":"uuids-to-vaccinations",
        "report.ecvList" : "uuid-to-ecv"
}
```
`report.vaccination.active` activates the individual vaccinations report to be usable when the module is loaded.
`report.vaccination.active` activates the ECV part of report to be usable when the module is loaded.
`report.prenatalVisitType` specifies the uuid  of prenatal visit to filter on.
`report.vaccinations` specifies the uuid of vaccination question concept.
`report.vaccinationSequenceNumber`specifies uuid of the vaccination sequence number concept.
`report.boosterSequenceNumber` specifies the uuid of the vaccination booster number concept.
`report.vaccinationList` specifies the uuids of individual vaccinations.
`report.ecvList` specifies the uuids of individual vaccinations for the full pediatric coverage.
Numbers can be added to the individual uuids of the vaccinations in the vaccinationList and ecvList for the booster or sequence number to search for.

The report template can be found at [MSPP: Vaccination](https://docs.google.com/spreadsheets/d/1Nf95GxnyDl-YNNAKLqXGYs34tO8FsmiLJqKInwd46FA/edit#gid=1608868809)