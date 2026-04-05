-- pop-and-issue-tokens.lua
-- 대기열에서 사용자를 꺼내고 입장 토큰을 발급하는 원자적 스크립트
--
-- KEYS[1] = queue:waiting (Sorted Set)
-- ARGV[1] = entry-token: (토큰 키 prefix)
-- ARGV[2] = count (꺼낼 인원 수)
-- ARGV[3] = ttl (토큰 만료 시간, 초)
-- ARGV[4..N] = 미리 생성된 UUID 토큰들
--
-- 반환: 토큰이 발급된 userId 목록

-- 1. ZPOPMIN으로 대기열에서 count명을 꺼낸다
local popped = redis.call('ZPOPMIN', KEYS[1], ARGV[2]);

--결과 테이블
local results = {}

for i = 1, #popped, 2 do
    local userId = popped[i];
    local tokenIdx = math.floor(i/2) + 4;
    local token = ARGV[tokenIdx];
    redis.call('SET', ARGV[1] .. userId, token, 'EX', ARGV[3]);
    table.insert(results, userId);
end

return results;