# Roles / access rights

> [!IMPORTANT]
> Using the REST API requires adding some _Identity_ to the user to authenticate to the backend, first.

## Member

A _User's_ default role is _member_. The member role itself allows to read Qualifications and Tools.

Additionally, a member is allowed to unlock the tools that are associated with the Qualifications they have as Member 
Qualifications (through the Device the Tool is attached to).

## Instructor

A _User_ can be _Instructor_ for one or multiple _Qualifications_. This role allows them to add these Qualifications as
member Qualifications to other Users.

## Administrator (Admin)

The _Administrator_ role allows for the following:
* adding, editing and deleting Devices, Tools, Qualifications, Users
* hard deleting Users
* attaching and detaching Tools to/from Devices
* remotely restarting a Device
* upgrading Device firmware
* adding and removing Identities to/from Users
* adding and removing an Instructor Qualification to/from a User (promoting/demoting a User as an Instructor for a Qualification)

> [!IMPORTANT]
> The Administrator role does **not** allow for adding a Member Qualification to a User. However, a User with 
> Administrator role can still achieve this: They first use their Administrator privileges to add the desired Qualification
> as Instructor Qualification to themselves. Now, they are an Instructor for that Qualification and can give out the 
> Qualification as Member Qualification. Finally, they can remove the Qualification as Instructor Qualification from
> themselves.
