## Antenatal Reports
This report searches different lab tests. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.antenatalGestation.active": "true",
    "report.antenatalRisks.active": "true"
}
```
`report.antenatalGestation.active` activates the antenatal gestation report to be usable when the module is loaded.
`report.antenatalRisks.active` activates the antenatal risks report to be usable when the module is loaded.
The report template can be found at [MSPP: Prise en charge de la mère](https://docs.google.com/spreadsheets/d/1ei2HVsgPDNBF4-M2eQilv_GSy3v_fSGwIMW9DrUDXQc/edit#gid=180349304)