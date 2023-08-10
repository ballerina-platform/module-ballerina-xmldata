import ballerina/xmldata;
import ballerina/io;

@xmldata:Name {
    value: "appointment"
}
type Appointment record {
    string firstName;
    string lastName;
    string email;
    string age;
};

@xmldata:Name {
    value: "appointments"
}
type Appointments record {
    Appointment[] appointment;
};

public function main() returns error? {
    xml xmlPayload = xml `<appointments><appointment><firstName>John</firstName><lastName>Doe</lastName><email>john.doe@gmail.com</email><age>28</age></appointment><appointment><firstName>John</firstName><lastName>Doe</lastName><email>john.doe@gmail.com</email><age>28</age></appointment></appointments>`;

    // Works okay
    map<json> result2 = check xmldata:fromXml(xmlPayload);
    io:println(result2);

    // Doesn't work. Failed with "error: The record type name: AppointmentPayload mismatch with given XML name: appointments"
    Appointments result3 = check xmldata:fromXml(xmlPayload);
    io:println(result3);
}
