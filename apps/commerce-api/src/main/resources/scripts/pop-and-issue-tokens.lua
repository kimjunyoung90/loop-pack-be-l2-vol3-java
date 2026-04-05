-- pop-and-issue-tokens.lua
-- 대기열에서 사용자를 꺼내고 입장 토큰 + 주문 가능 시각을 발급하는 원자적 스크립트
--
-- KEYS[1] = queue:waiting (Sorted Set)
-- ARGV[1] = entry-token: (토큰 키 prefix)
-- ARGV[2] = orderable-at: (주문 가능 시각 키 prefix)
-- ARGV[3] = count (꺼낼 인원 수)
-- ARGV[4] = ttl (토큰 만료 시간, 초)
-- ARGV[5..5+count-1] = 미리 생성된 UUID 토큰들
-- ARGV[5+count..5+2*count-1] = 유저별 orderableAt 타임스탬프(ms)
--
-- 반환: 토큰이 발급된 userId 목록

-- 1. ZPOPMIN으로 대기열에서 count명을 꺼낸다
local count = tonumber(ARGV[3])
local popped = redis.call('ZPOPMIN', KEYS[1], count)

local results = {}

for i = 1, #popped, 2 do
    local userId = popped[i]
    local idx = math.floor(i / 2) + 1
    local token = ARGV[4 + idx]
    local orderableAt = ARGV[4 + count + idx]

    -- 2. 입장 토큰 저장
    redis.call('SET', ARGV[1] .. userId, token, 'EX', ARGV[4])
    -- 3. 주문 가능 시각 저장
    redis.call('SET', ARGV[2] .. userId, orderableAt, 'EX', ARGV[4])

    table.insert(results, userId)
end

return results
