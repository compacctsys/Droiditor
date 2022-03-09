
select cast(a.ts/1e9 as int), charge_uah, capacity_pct, current_ua
from
(
	select ts, value as charge_uah
	from counter
	where track_id = 0
) as a
 join
(
	select ts, value as capacity_pct
	from counter
	where track_id = 1
)  as b
on a.ts = b.ts
join
(
	select ts, value as current_ua
	from counter
	where track_id = 2
) as c
on a.ts = c.ts