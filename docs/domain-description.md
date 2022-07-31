# Domain Description

The following describes the scenario for which fabX is developed.

## General

A new user comes to a fab lab / makerspace / etc. and wants to use a tool, e.g. a laser cutter. To ensure that the user
can safely use the tool, the operator of the space offers a course for this tool. Only users who have received the
qualification, showing that they attended the course, are able to use this tool.

The user chooses to book the course and attends it. If the user has done well enough throughout the course, they receive
a qualification for this tool at the end.

The instructor of the course enters into the system that the user has received the qualification for the laser cutter.

Now, the user can identify themselves to the tool and unlock/use the tool.

## Terminology

* User: A person who uses tools.
* Card: A physical card which can be used to personally identify a user, e.g. an NFC tag.
* Tool: A machine/tool offered to users, e.g. a laser cutter.
* Device: The controller device that tools can be attached to for access control.
* Qualification: Proof that a certain course has been taken by a user.
* Admin: An administrator of the system.

## Tools

Different tools are available at the space. This includes laser cutters and woodworking tools.

For some tools, e.g. laser cutters, it has to be ensured that the user is always nearby the tool. The user has to
continuously or repeatedly identify themselves to the tool.

Other tools, e.g. woodworking tools, are stored in drawers with electronic locks, similar to parcel delivery machines.
The electronic locks require a short pulse to unlock.

## Qualifications

A qualification is given out after a user has attended a course. This course may be longer or shorter, depending on
which tool(s) the course is for.

A qualification may allow a user to use a single or multiple tools.

Some part of a tool may change in the future, requiring

## Devices

To not require too many devices, multiple tools can be attached to a single device.

When a user identifies to a device, they can choose one of the tools available to them (based on the user's
qualifications and the qualifications required for the attached tools).