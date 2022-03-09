
select cast(ts/1e9 as int) as time, sum(dur)/1e9 / 8 as cpu_time_sec 
from sched 
where utid!=0
group by time
