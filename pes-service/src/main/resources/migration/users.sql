SELECT
u1.name, u1.uname, u1.email
FROM
stela_users u1, stela_groups_users_link gul1
WHERE
u1.uid = gul1.uid AND
FROM_UNIXTIME(u1.last_login) >= NOW() - INTERVAL 1 year AND
groupid = (
	SELECT groupid
	FROM stela_groups
	WHERE stela_groups.name LIKE "%stela helios%"
) AND
u1.uid IN (
	SELECT u2.uid
    FROM stela_users u2, stela_groups_users_link gul2
    WHERE u2.uid = gul2.uid {{groupIds}}
)