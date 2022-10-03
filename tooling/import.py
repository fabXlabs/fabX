#!/usr/bin/env python3

import json
import requests
from requests.auth import HTTPBasicAuth

# SETUP
baseUrlOld = "https://old.example.com/api/v1"
basicOld = HTTPBasicAuth('admin1', 'demopassword')

baseUrlNew = "http://new.example.com/api/v1"
basicNew = HTTPBasicAuth('admin', 'password')

print("hello world")

# GET QUALIFICATIONS OLD
qualificationsOld = requests.get(f'{baseUrlOld}/qualification', auth=basicOld).json()

# GET DEVICES OLD
devicesOld = requests.get(f'{baseUrlOld}/device', auth=basicOld).json()
devicesOld.sort(key=lambda e: e['id'])

# GET TOOLS OLD
toolsOld = requests.get(f'{baseUrlOld}/tool', auth=basicOld).json()
toolsOld.sort(key=lambda e: e['id'])

# GET USERS OLD
usersOld = requests.get(f'{baseUrlOld}/user', auth=basicOld).json()
usersOld.sort(key=lambda e: e['id'])


# CREATE QUALIFICATIONS NEW
qualificationIdsNew = {} # old id -> new id
for q in qualificationsOld:
	print(f'creating qualification {q["name"]}')
	response = requests.post(f'{baseUrlNew}/qualification', json={
		'name': q['name'],
		'description': q['description'],
		'colour': q['colour'],
		'orderNr': q['orderNr']
	}, auth=basicNew)
	print(response.status_code)
	qualificationIdsNew[q['id']] = response.text

# MAKE ACTING ADMIN INSTRUCTOR FOR ALL QUALIFICATIONS
meIdNew = requests.get(f'{baseUrlNew}/user/me', auth=basicNew).json()['id']
print(f"meId: {meIdNew}")

for q in qualificationsOld:
	print(f'make me instructor {q["name"]}')
	response = requests.post(f'{baseUrlNew}/user/{meIdNew}/instructor-qualification', json={
		'qualificationId': qualificationIdsNew[q['id']]
	}, auth=basicNew)
	print(response.status_code)

# CREATE DEVICES NEW
deviceIdsNew = {} # old id -> new id
for d in devicesOld:
	print(f'creating device {d["name"]}')
	response = requests.post(f'{baseUrlNew}/device', json={
		'name': d['name'],
		'background': d['bgImageUrl'],
		'backupBackendUrl': d['backupBackendUrl'],
		'mac': d['mac'],
		'secret': d['secret'],
	}, auth=basicNew)
	print(response.status_code)
	deviceIdsNew[d['id']] = response.text

# CREATE TOOLS NEW
for t in toolsOld:
	print(f'creating tool {t["name"]}')
	response = requests.post(f'{baseUrlNew}/tool', json={
		'name': t['name'],
		'type': t['toolType'],
		'requires2FA': False,
		'time': t['time'],
		'idleState': t['idleState'],
		'wikiLink': t['wikiLink'],
		'requiredQualifications': [qualificationIdsNew[q['id']] for q in t['qualifications']],
	}, auth=basicNew)
	print(response.status_code)
	toolIdNew = response.text

	# attach tool to device
	response = requests.put(f'{baseUrlNew}/device/{deviceIdsNew[t["deviceId"]]}/attached-tool/{t["pin"]}', json={
		'toolId': toolIdNew
	}, auth=basicNew)
	print(response.status_code)

# CREATE USERS NEW
for u in usersOld:
	# create user
	print(f'creating user {u["firstName"]} {u["lastName"]}')
	response = requests.post(f'{baseUrlNew}/user', json={
		'firstName': u['firstName'],
		'lastName': u['lastName'],
		'wikiName': u['wikiName']
	}, auth=basicNew)
	print(response.status_code)
	userIdNew = response.text

	# add card identity
	response = requests.post(f'{baseUrlNew}/user/{userIdNew}/identity/card', json={
		'cardId': u['cardId'],
		'cardSecret': u['cardSecret']
	}, auth=basicNew)
	print(response.status_code)

	# add phone nr identity
	response = requests.post(f'{baseUrlNew}/user/{userIdNew}/identity/phone', json={
		'phoneNr': u['phoneNumber']
	}, auth=basicNew)
	print(response.status_code)

	# add member qualifications
	for q in u['qualifications']:
		print(f'adding qualification {q["name"]} ({q["id"]} -> {qualificationIdsNew[q["id"]]})')
		response = requests.post(f'{baseUrlNew}/user/{userIdNew}/member-qualification', json={
			'qualificationId': qualificationIdsNew[q['id']]
		}, auth=basicNew)
		print(response.status_code)













