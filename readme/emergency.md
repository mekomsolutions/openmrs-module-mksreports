## Emergency report
This is a MSPP  Statistic Report on emergency cases.

**Setup**

It is setup by setting the following properties in the [initializer](https://github.com/mekomsolutions/openmrs-module-initializer) `jsonkeyvalues`' domain configuration. 

```bash
{
    ...
    ...,
    "report.emergency.active " : "true",
    "report.emergency.question.concept" : "uuid-to-emergency-question-concept",
    "report.emergency.roadAccidents.concept" : "uuid-to-road-accidentTypes-setConcept",
    "report.emergency.workAccident.concept" : "uuid-to-work-accident-concept",
    "report.emergency.sexualViolence.concept" : "uuid-to-sexualViolence-concept",
    "report.emergency.physicalViolence.concept" : "uuid-to-physicalViolence-concept",
    "report.emergency.otherViolenceType.concept" : "uuid-to-otherViolence-types-concept",
    "report.emergency.medicalAndSurgicalEmergenciesQuesion.concept" : "uuid-to-medicalAndSurgicalEmergency-question-concept",
    "report.emergency.medicalAndSurgicalEmergenciesSetOfSets.concept" : "uuid-to-medicalAndSurgicalEmergency-superSet-concept",
    "report.emergency.otherEmergencies.concept" : "uuid-to-otherEmergencies-concept",
    "report.emergency.otherEmergenciesQuestion.concept" : "uuid-to-otherEmergencies-question-concept",
    "report.emergency.referredTo.concept" : "uuid-to-referredTo-concept",
    "report.emergency.leftWithoutPermission.concept" : "uuid-to-leftWithoutPermission-concept",
    "report.emergency.yes.concept" : "uuid-to-yes-answer-concept"
}
```
`report.emergency.active` activates the report to be usable when the module is loaded.

`report.emergency.question.concept` specifies the question answered by the type of emergency, it could be the Reason for consultation question concept.

`report.emergency.roadAccidents.concept` specifies a concept set defining the types of road accidents to report on. The concepts specified should be answering the concept defined by the `report.emergency.question.concept`.

`report.emergency.workAccident.concept` specifies a concept defining a work accident emergency. It should be answering the concept defined by the `report.emergency.question.concept`.

`report.emergency.physicalViolence.concept` specifies a concept defining a physical violence emergency. It should be answering the concept defined by the `report.emergency.question.concept`.

`report.emergency.sexualViolence.concept` specifies a concept defining sexual violence emergency. It should be answering the concept defined by the `report.emergency.question.concept`.

`report.emergency.otherViolenceType.concept` specifies a concept defining other violence type emergency. It should be answering the concept defined by the `report.emergency.question.concept` property.

`report.emergency.medicalAndSurgicalEmergenciesQuesion.concept` specifies a coded question answered by the coded answers specified by the `report.emergency.medicalAndSurgicalEmergenciesSetOfSets.concept` property.

`report.emergency.medicalAndSurgicalEmergenciesSetOfSets.concept` specifies a concept set of Medical and Surgical emergency categories. These categories are sets defined with coded set members answering the question defined by `report.emergency.medicalAndSurgicalEmergenciesQuesion.concept`.

`report.emergency.otherEmergenciesQuestion.concept` specifies a coded question answered by the coded answers specified by the `report.emergency.otherEmergencies.concept` property.

`report.emergency.otherEmergencies.concept` specifies a set of coded set members answering the question defined by `report.emergency.otherEmergenciesQuestion.concept` property.

`report.emergency.referredTo.concept` specifies a coded question concept which allows for determining referred outcome cases.

`report.emergency.leftWithoutPermission.concept` specifies a coded question concept which allows for determining left without permission outcome cases. Trues cases are determined by the coded answer defined by `report.emergency.yes.concept` property. 

`report.emergency.yes.concept` specifies a `Yes`/`True` coded answer concept answering the the questino defined by `report.emergency.leftWithoutPermission.concept` property.

**Note:** “Deceased” outcome cases take precedence over the other outcomes, followed by "Left without permission", "Referred", "Cared for" outcomes respectively.