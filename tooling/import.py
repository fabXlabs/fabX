#!/usr/bin/env python3
import requests
from loguru import logger
from requests import Response
from requests.auth import HTTPBasicAuth
from typing import Any, TypeVar, Iterable

# SETUP
baseUrlOld = "https://old.example.com/api/v1"
basicOld = HTTPBasicAuth('admin1', 'demopassword')

baseUrlNew = "http://new.example.com/api/v1"
basicNew = HTTPBasicAuth('admin', 'password')

logger.debug("hello world")

T = TypeVar("T")


def first(iterable: Iterable[T], condition=lambda x: True) -> T | None:
    try:
        return next(x for x in iterable if condition(x))
    except StopIteration:
        return None


# OLD: GET QUALIFICATIONS
qualificationsOld = requests.get(f"{baseUrlOld}/qualification", auth=basicOld).json()

# OLD: GET DEVICES
devicesOld = requests.get(f"{baseUrlOld}/device", auth=basicOld).json()
devicesOld.sort(key=lambda e: e["id"])

# OLD: GET TOOLS
toolsOld = requests.get(f"{baseUrlOld}/tool", auth=basicOld).json()
toolsOld.sort(key=lambda e: e["id"])

# OLD: GET USERS
usersOld = requests.get(f"{baseUrlOld}/user", auth=basicOld).json()
usersOld.sort(key=lambda e: e["id"])

# NEW: GET ACTOR INFO
me = requests.get(f"{baseUrlNew}/user/me", auth=basicNew).json()
meIdNew = me["id"]
logger.info(f"meId: {meIdNew}")

# NEW: GET QUALIFICATIONS
qualificationsNew: list[dict[str, Any]] = requests.get(f"{baseUrlNew}/qualification", auth=basicNew).json()

# NEW: GET DEVICES
devicesNew: list[dict[str, Any]] = requests.get(f"{baseUrlNew}/device", auth=basicNew).json()

# NEW: GET TOOLS
toolsNew: list[dict[str, Any]] = requests.get(f"{baseUrlNew}/tool", auth=basicNew).json()

# NEW: GET USERS
usersNew: list[dict[str, Any]] = requests.get(f"{baseUrlNew}/user", auth=basicNew).json()

# MAPPING
qualificationIdsNew = {}  # old id -> new id
deviceIdsNew = {}  # old id -> new id


def check_response(response: Response):
    if 200 <= response.status_code < 300:
        logger.success(response.status_code)
    elif response.status_code >= 400:
        logger.error(response.status_code)


def create_qualification(old_qualification: dict[str, Any]) -> str:
    logger.info(f'creating qualification {old_qualification["name"]}')
    response = requests.post(f'{baseUrlNew}/qualification', json={
        "name": old_qualification["name"],
        "description": old_qualification["description"],
        "colour": old_qualification["colour"],
        "orderNr": old_qualification["orderNr"]
    }, auth=basicNew)
    check_response(response)
    qualificationsNew.append(requests.get(f"{baseUrlNew}/qualification/{response.text}", auth=basicNew).json())
    return response.text


def make_actor_instructor(qualification_id: str):
    logger.info(f'make me instructor {q["name"]}')
    response = requests.post(f"{baseUrlNew}/user/{meIdNew}/instructor-qualification", json={
        "qualificationId": qualification_id
    }, auth=basicNew)
    check_response(response)


def create_device(old_device: dict[str, Any]) -> str:
    logger.info(f'creating device {old_device["name"]}')
    response = requests.post(f"{baseUrlNew}/device", json={
        "name": old_device["name"],
        "background": old_device["bgImageUrl"],
        "backupBackendUrl": old_device["backupBackendUrl"],
        "mac": old_device["mac"],
        "secret": old_device["secret"],
    }, auth=basicNew)
    check_response(response)
    devicesNew.append(requests.get(f"{baseUrlNew}/device/{response.text}", auth=basicNew).json())
    return response.text


def create_tool(old_tool: dict[str, Any]) -> str:
    logger.info(f'creating tool {old_tool["name"]}')
    response = requests.post(f'{baseUrlNew}/tool', json={
        "name": old_tool["name"],
        "type": old_tool["toolType"],
        "requires2FA": False,
        "time": old_tool["time"],
        "idleState": old_tool["idleState"],
        "wikiLink": old_tool["wikiLink"],
        "requiredQualifications": [qualificationIdsNew[q["id"]] for q in old_tool["qualifications"]],
    }, auth=basicNew)
    check_response(response)
    toolsNew.append(requests.get(f"{baseUrlNew}/tool/{response.text}", auth=basicNew).json())
    return response.text


def attach_tool(tool_id: str, device_id: str):
    logger.info(f"attaching tool {tool_id} to device {device_id} (pin: {t['pin']})")
    response = requests.put(f'{baseUrlNew}/device/{deviceIdsNew[t["deviceId"]]}/attached-tool/{t["pin"]}', json={
        "toolId": toolIdNew
    }, auth=basicNew)
    check_response(response)


def create_user(old_user: dict[str, Any]) -> str:
    logger.info(f'creating user {old_user["firstName"]} {old_user["lastName"]}')
    response = requests.post(f"{baseUrlNew}/user", json={
        "firstName": old_user['firstName'],
        "lastName": old_user['lastName'],
        "wikiName": old_user['wikiName']
    }, auth=basicNew)
    check_response(response)
    usersNew.append(requests.get(f"{baseUrlNew}/user/{response.text}", auth=basicNew).json())
    return response.text


def add_card_identity(user_id: str, card_id: str, card_secret: str):
    logger.info(f"adding card identity {card_id} to {user_id}")
    response = requests.post(f"{baseUrlNew}/user/{user_id}/identity/card", json={
        "cardId": card_id,
        "cardSecret": card_secret
    }, auth=basicNew)
    check_response(response)


def add_phone_nr_identity(user_id: str, phone_nr: str):
    logger.info(f"adding phone nr identity {phone_nr} to {user_id}")
    response = requests.post(f"{baseUrlNew}/user/{user_id}/identity/phone", json={
        "phoneNr": phone_nr
    }, auth=basicNew)
    check_response(response)


def add_member_qualification(user_id: str, qualification_id: str):
    logger.info(f'adding qualification {qualification_id} to user {user_id}')
    response = requests.post(f'{baseUrlNew}/user/{user_id}/member-qualification', json={
        'qualificationId': qualification_id
    }, auth=basicNew)
    check_response(response)


def remove_member_qualification(user_id: str, qualification_id: str):
    logger.info(f'removing qualification {qualification_id} from user {user_id}')
    response = requests.delete(f'{baseUrlNew}/user/{user_id}/member-qualification/{qualification_id}', auth=basicNew)
    check_response(response)


# QUALIFICATIONS NEW
logger.debug("importing qualifications")
for q in qualificationsOld:
    existing_new_qualification = first(qualificationsNew, lambda x: x["name"] == q["name"])
    if existing_new_qualification:
        qualificationIdsNew[q["id"]] = existing_new_qualification["id"]
        # TODO diff qualification
    else:
        qualificationIdsNew[q["id"]] = create_qualification(q)

# MAKE ACTING ADMIN INSTRUCTOR FOR ALL QUALIFICATIONS
logger.debug("make acting admin instructor")
for q in qualificationsOld:
    qualificationIdNew = qualificationIdsNew[q["id"]]
    if qualificationIdNew not in (me["instructorQualifications"] or []):
        make_actor_instructor(qualificationIdNew)

# CREATE DEVICES NEW
logger.debug("importing devices")
for d in devicesOld:
    existing_new_device = first(devicesNew, lambda x: x["name"] == d["name"])
    if existing_new_device:
        deviceIdsNew[d["id"]] = existing_new_device["id"]
        # TODO diff device
    else:
        deviceIdsNew[d["id"]] = create_device(d)

# CREATE TOOLS NEW
for t in toolsOld:
    existing_new_tool = first(toolsNew, lambda x: x["name"] == t["name"])
    if existing_new_tool:
        toolIdNew = existing_new_tool["id"]
        # TODO diff tool
    else:
        toolIdNew = create_tool(t)

    # attach tool to device
    deviceIdNew = deviceIdsNew[t["deviceId"]]
    deviceNew = first(devicesNew, lambda x: x["id"] == deviceIdNew)
    if str(t["pin"]) in deviceNew["attachedTools"].keys():
        if deviceNew["attachedTools"][str(t["pin"])] != toolIdNew:
            logger.error(f'Tool {deviceNew["attachedTools"][str(t["pin"])]} '
                         f'attached at pin {t["pin"]} of device {deviceIdNew} '
                         f'instead of Tool {t["name"]}')
            # TODO detach other tool, attach t
    else:
        attach_tool(toolIdNew, deviceIdNew)

# CREATE USERS NEW
logger.debug("importing users")
for u in usersOld:
    new_user = first(usersNew, lambda x: x["wikiName"] == u["wikiName"])
    if new_user:
        userIdNew = new_user["id"]
        # TODO diff user
    else:
        userIdNew = create_user(u)
        new_user = first(usersNew, lambda x: x["wikiName"] == u["wikiName"])

    if u["cardId"]:
        existing_card_identity = first(
            new_user["identities"],
            lambda x: x["type"] == "cloud.fabX.fabXaccess.user.rest.CardIdentity",
        )
        if existing_card_identity:
            if existing_card_identity["cardId"] != u["cardId"] or existing_card_identity["cardSecret"] != u["cardSecret"]:
                logger.error(f'Card {existing_card_identity["cardId"]} set for user {u["wikiName"]} '
                             f'instead of {u["cardId"]} (or secret may differ)')
        else:
            add_card_identity(userIdNew, u['cardId'], u['cardSecret'])

    if u["phoneNumber"]:
        existing_phone_nr_identity = first(
            new_user["identities"],
            lambda x: x["type"] == "cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity" and x["phoneNr"] == u["phoneNumber"]
        )
        if not existing_phone_nr_identity:
            add_phone_nr_identity(userIdNew, u['phoneNumber'])

    for q in u["qualifications"]:
        if qualificationIdsNew[q['id']] not in new_user["memberQualifications"]:
            add_member_qualification(userIdNew, qualificationIdsNew[q['id']])

    for q_id in new_user["memberQualifications"]:
        if q_id not in {qualificationIdsNew[q_old["id"]] for q_old in u["qualifications"]}:
            remove_member_qualification(userIdNew, q_id)
