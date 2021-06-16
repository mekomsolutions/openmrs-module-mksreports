## Lab
This report searches different lab tests. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.lab.active" : "true"
}
```
`report.lab.active` activates the report to be usable when the module is loaded.
The report template can be found at [MSPP: Examens de laboratoire](https://docs.google.com/spreadsheets/d/1VbwplLxWZlnsHWo95OXUVU14zEkFKIOnwFe-7xx_nNQ/edit#gid=111645596)