import type { Qualification } from '$lib/api/model/qualification';

export function augmentQualifications(
	qualifications: Qualification[]
): (qualifications: string[]) => Qualification[] {
	const qualificationsMap = new Map(qualifications.map((q) => [q.id, q]));

	function getQualifications(qualifications: string[]) {
		return qualifications
			.map((q) => qualificationsMap.get(q))
			.filter((q): q is Qualification => !!q)
			.sort((a: Qualification, b: Qualification) => {
				return a.orderNr - b.orderNr;
			});
	}
	return getQualifications;
}
