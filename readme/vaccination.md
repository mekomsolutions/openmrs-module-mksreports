## Vaccination
This report searches different lab tests. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.vaccination.active": "true",
    "report.ecv.active": "true"
}
```
`report.vaccination.active` activates the individual vaccinations report to be usable when the module is loaded.
`report.vaccination.active` activates the ECV part of report to be usable when the module is loaded.

The report template can be found at [MSPP: Vaccination](https://docs.google.com/spreadsheets/d/1Nf95GxnyDl-YNNAKLqXGYs34tO8FsmiLJqKInwd46FA/edit#gid=1608868809)