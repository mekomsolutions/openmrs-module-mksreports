## Family Planning
This report searches different lab tests. It is a MSPP Report requested by the Haitian MoH to any clinic in Haiti.

**Setup**

It is setup by setting the following properties in [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.familyPlanning.active" : "true"
}
```
`report.familyPlanning.active` activates the report to be usable when the module is loaded.
The report template can be found at [MSPP 'Clients PF' report](https://docs.google.com/spreadsheets/d/13A3gBRwi45-YwnArNsDgQB4EPVwsTswp/edit#gid=906556663)