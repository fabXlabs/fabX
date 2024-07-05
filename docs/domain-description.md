# Domain Description

The following describes the scenario for which fabX is built.

## General

A person comes to a fab lab / makerspace / etc. and wants to use a _tool_, e.g. a laser cutter. To ensure that a 
space _member_ can safely use the tool, the operator of the space offers a course for this tool. Only members who have 
received the _qualification_, showing that they attended the course, are allowed (and able) to use this _tool_.

The member chooses to book the course and attends it. If the member has done well enough throughout the course, they
receive a _qualification_ for this _tool_.

The _instructor_ of the course enters into the system that the member has received the _qualification_ for the _tool_.

Now, the member can identify themselves to the _device_ where the _tool_ is attached, using their _card_, and use 
the _tool_.

## Terminology

* _User_: A person (may be Member, Instructor, Admin or a combination) registered in the system.
* _Member_: A person who uses _tools_.
* _Instructor_: A person teaching at least one course for one or more _tools_.
* _Card_: A physical card which can be used to personally identify a user, e.g. an NFC tag.
* _Tool_: A machine/tool offered to _members_, e.g. a laser cutter. Might require one or more _qualifications_ for usage. 
* _Device_: The controller device that _tools_ can be attached to for access control.
* _Qualification_: Proof that a certain course has been taken by a _member_.
* _Admin_: An administrator of the system.

## Tools

Different _tools_ are available at the space. This includes, e.g., laser cutters and woodworking tools.

For some _tools_, e.g. laser cutters, it has to be ensured that the user is always nearby the tool. The user has to
continuously or repeatedly identify themselves to the _device_ where the _tool_ is attached.

Other _tools_, e.g. woodworking tools, may be stored in drawers with electronic locks, similar to parcel delivery 
machines. The electronic locks require a short pulse to unlock.

## Qualifications

A _qualification_ is given out once a _member_ has attended a course. This course may be longer or shorter, depending on
the _tool(s)_ the course is about.

A _qualification_ may allow a member to use a single or multiple _tools_.

Some part of a _tool_ may change in the future, requiring a follow-up course detailing the changes. This can be modelled
using a secondary _qualification_. Once the change is applied, the secondary _qualification_ can be additionally 
required for using the _tool_.

## Devices

To not require too many _devices_, multiple _tools_ can be attached to a single _device_.

When a _member_ identifies to a _device_, they can choose one of the _tools_ available to them (based on the _member's_
_qualifications_ and the _qualifications_ required for the attached _tools_).
