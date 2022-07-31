# Domain Module

## Domain Model

* User: a natural person
  * can have multiple UserIdentities
* Device: a client device
  * has a single DeviceIdentity

### Authentication

* Identity: An identification of a User/Device/...
* UserIdentity: An identification of a User
  * UserPasswordIdentity: Combination of username and password (hash) identifying a User
  * CardIdentity: A single secret identifying a User (at a Device)
  * PhoneNumberIdentity: A phone number identifying a User (at a Device)
  * (WebAuthnIdentity)
* DeviceIdentity: Combination of mac and secret identifying a Device

### Authorization

* Actor: An acting party, e.g. User or Device, but might also be a bot or someone/something different in the future
* AdminActor: An admin user
* InstructorActor: An instructor user
* MemberActor: A member user
* DeviceActor: A device
  * sometimes acts on behalf of / as a MemberActor