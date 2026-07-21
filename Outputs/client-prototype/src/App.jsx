import { useMemo, useState } from "react";
import {
  ArrowRight, Basketball, BatteryHigh, Bell, CalendarDots, CaretDown,
  CellSignalFull, CheckCircle, CreditCard, Crosshair, CurrencyCircleDollar,
  DiceFive, EnvelopeSimple, Eye, EyeSlash, GearSix, HandCoins, Headset,
  House, IdentificationCard, ListChecks, LockKey, Minus, PaperPlaneTilt,
  Plus, Receipt, ShareNetwork, ShieldCheck, SoccerBall, Storefront, Ticket,
  Trophy, User, UserCircleGear, UsersThree, Wallet, WechatLogo, WifiHigh, X,
} from "@phosphor-icons/react";

const lotteryItems = [
  { name: "竞彩足球", meta: "8场赛事可投", icon: SoccerBall, tone: "green" },
  { name: "竞彩篮球", meta: "3场赛事可投", icon: Basketball, tone: "cyan" },
  { name: "胜负彩", meta: "竞猜14场赛事", icon: Trophy, tone: "blue" },
  { name: "任选九", meta: "14场任选9场", icon: DiceFive, tone: "purple" },
  { name: "大乐透", meta: "奖池7.84亿", icon: CurrencyCircleDollar, tone: "red" },
  { name: "排列三", meta: "猜3位赢千元", icon: Ticket, tone: "amber" },
];

const serviceItems = [
  ["合买记录", UsersThree], ["追号记录", Crosshair], ["保存记录", Ticket], ["店铺信息", Storefront],
  ["我的红包", Wallet], ["我的消息", Bell], ["推送设置", PaperPlaneTilt], ["账号管理", UserCircleGear],
  ["分享好友", ShareNetwork], ["我的资料", IdentificationCard], ["联系店主", Headset],
];

const matches = [
  { id: 201, league: "韩职", time: "45++", home: "济州SK", away: "江原FC", score: "1-1", half: "上半场 1-1", live: true, odds: ["4.62", "3.05", "1.71"] },
  { id: 202, league: "韩职", time: "45++", home: "全北现代", away: "大田市民", score: "0-0", half: "上半场 0-0", live: true, odds: ["1.77", "3.28", "3.85"] },
  { id: 203, league: "韩职", time: "中场", home: "蔚山现代", away: "仁川联", score: "1-1", half: "上半场 1-1", live: true, odds: ["2.02", "2.85", "3.53"] },
  { id: 205, league: "欧冠", time: "07-22 00:00", home: "萨巴赫", away: "库奥皮奥", score: "VS", half: "未开赛", live: false, odds: ["1.44", "4.10", "5.25"] },
  { id: 206, league: "欧冠", time: "07-22 01:00", home: "奥胡斯", away: "波兹南", score: "VS", half: "未开赛", live: false, odds: ["2.36", "3.15", "2.82"] },
];

function StatusBar() {
  return <div className="status-bar" aria-hidden="true"><span>19:22</span><div><CellSignalFull weight="fill" /><WifiHigh weight="bold" /><BatteryHigh weight="fill" /></div></div>;
}

function IconButton({ icon: Icon, label, tone = "blue", onClick }) {
  return <button className="quick-action" onClick={onClick} type="button"><span className={`quick-icon ${tone}`}><Icon weight="fill" /></span><span>{label}</span></button>;
}

function HomeScreen({ notify, onGoMatches }) {
  const [joinMode, setJoinMode] = useState("合买大厅");
  return <div className="screen-scroll home-screen">
    <header className="home-heading">
      <div><span className="eyebrow">线下店铺</span><strong>实体出票</strong></div>
      <button type="button" className="safe-badge" onClick={() => notify("已开启理性购彩提醒")}><ShieldCheck weight="fill" /><span>放心玩</span></button>
    </header>
    <section className="hero-crop" aria-label="2026体育赛事活动"><img src="/reference-home.jpg" alt="2026体育赛事活动宣传" /></section>
    <section className="wallet-card surface-card">
      <div className="balance-block"><span>账户余额</span><strong>¥ 8,101.84</strong></div>
      <div className="wallet-actions">
        <IconButton icon={Headset} label="投诉" tone="amber" onClick={() => notify("投诉入口已打开")} />
        <IconButton icon={Storefront} label="店主" tone="blue" onClick={() => notify("已为你联系店主")} />
        <IconButton icon={WechatLogo} label="微信" tone="green" onClick={() => notify("分享卡片已准备")} />
        <IconButton icon={EnvelopeSimple} label="邀请" tone="purple" onClick={() => notify("邀请码已复制")} />
      </div>
    </section>
    <div className="hall-switch" role="group" aria-label="大厅类型">
      {[["合买大厅", UsersThree, "参与发起合买"], ["跟单大厅", Receipt, "发单赚佣金"]].map(([label, Icon, sub]) => <button type="button" key={label} className={joinMode === label ? "active" : ""} onClick={() => { setJoinMode(label); notify(`已切换至${label}`); }}><Icon weight="bold" /><span><b>{label}</b><small>{sub}</small></span></button>)}
    </div>
    <section className="lottery-section surface-card">
      <div className="section-heading"><div><span className="brand-spark">✦</span><h2>体育 · 福彩</h2></div><button type="button" onClick={onGoMatches}>全部 <ArrowRight /></button></div>
      <div className="lottery-grid">
        {lotteryItems.map(({ name, meta, icon: Icon, tone }) => <button type="button" className="lottery-item" key={name} onClick={() => name.includes("竞彩") ? onGoMatches() : notify(`${name}玩法即将开放`)}><span className={`lottery-icon ${tone}`}><Icon weight="fill" /></span><strong>{name}</strong><small>{meta}</small></button>)}
      </div>
      <button className="collapse-link" type="button" onClick={() => notify("已展示当前全部彩种")}>收起全部 <CaretDown /></button>
    </section>
  </div>;
}

function MatchCard({ match, selected, onPick }) {
  const labels = ["胜", "平", "负"];
  return <article className="match-card surface-card">
    <div className="match-meta"><span>周二{match.id} · {match.league}</span><span className={match.live ? "live-time" : "scheduled"}>{match.time}</span>{match.live ? <span className="live-label">正在直播 <span className="live-dot" /></span> : <span className="scheduled">未开赛</span>}</div>
    <div className="match-teams"><div><span className="team-mark"><ShieldCheck weight="fill" /></span><strong>{match.home}</strong></div><div className="score"><strong>{match.score}</strong><small>{match.half}</small></div><div><span className="team-mark"><ShieldCheck weight="fill" /></span><strong>{match.away}</strong></div></div>
    <div className="odds-row"><span>竞彩SP</span>{match.odds.map((odd, index) => { const key = `${match.id}-${index}`; return <button type="button" key={key} className={selected.includes(key) ? "selected" : ""} onClick={() => onPick(key)}><small>{labels[index]}</small>{odd}</button>; })}</div>
  </article>;
}

function MatchesScreen({ notify }) {
  const [sport, setSport] = useState("足球");
  const [type, setType] = useState("竞彩");
  const [date, setDate] = useState("今天");
  const [selected, setSelected] = useState([]);
  const [slipOpen, setSlipOpen] = useState(false);
  const [stake, setStake] = useState(20);
  const [submitted, setSubmitted] = useState(false);
  const visibleMatches = sport === "足球" ? matches : [];
  const potential = useMemo(() => ((stake || 0) * Math.max(selected.length, 1) * 1.71).toFixed(2), [stake, selected.length]);
  const togglePick = (key) => { setSelected((old) => old.includes(key) ? old.filter((item) => item !== key) : [...old, key]); setSlipOpen(true); setSubmitted(false); };
  return <div className="match-screen">
    <div className="match-top">
      <div className="sport-tabs">{["足球", "篮球"].map((item) => <button type="button" key={item} className={sport === item ? "active" : ""} onClick={() => setSport(item)}>{item}</button>)}<button type="button" onClick={() => notify("开奖公告即将开放")}>开奖公告</button></div>
      <div className="type-tabs">{["竞彩", "北单", "足彩"].map((item) => <button type="button" key={item} className={type === item ? "active" : ""} onClick={() => { setType(item); if (item !== "竞彩") notify(`${item}数据准备中`); }}>{item}</button>)}</div>
      <div className="date-tabs">{["周一\n07/20", "今天\n07/21", "周三\n07/22", "周四\n07/23"].map((item) => { const key = item.split("\n")[0]; return <button type="button" key={item} className={date === key ? "active" : ""} onClick={() => setDate(key)}>{item.split("\n").map((line) => <span key={line}>{line}</span>)}</button>; })}</div>
    </div>
    <div className="match-list screen-scroll">{visibleMatches.length ? visibleMatches.map((match) => <MatchCard key={match.id} match={match} selected={selected} onPick={togglePick} />) : <div className="empty-state"><Basketball weight="duotone" /><strong>暂无篮球赛事</strong><span>切回足球看看今天的比赛</span></div>}</div>
    {selected.length > 0 && !slipOpen && <button className="slip-fab" type="button" onClick={() => setSlipOpen(true)}><Ticket weight="fill" /> 投注单 · {selected.length}</button>}
    {slipOpen && selected.length > 0 && <div className="bet-slip" role="dialog" aria-label="投注单">
      <div className="slip-head"><strong>投注单</strong><button type="button" onClick={() => setSlipOpen(false)}><X /></button></div>
      {submitted ? <div className="submit-success"><CheckCircle weight="fill" /><strong>模拟投注已提交</strong><span>等待线下门店确认出票</span></div> : <><div className="slip-summary"><span>已选 {selected.length} 个结果</span><button type="button" onClick={() => setSelected([])}>清空</button></div><div className="stake-row"><span>投注金额</span><div><button type="button" onClick={() => setStake(Math.max(2, stake - 2))}><Minus /></button><input aria-label="投注金额" inputMode="numeric" value={stake} onChange={(e) => setStake(Number(e.target.value.replace(/\D/g, "")) || 0)} /><button type="button" onClick={() => setStake(stake + 2)}><Plus /></button></div></div><div className="return-row"><span>预计最高返还</span><strong>¥ {potential}</strong></div><button className="primary-button" type="button" onClick={() => setSubmitted(true)}>确认模拟投注</button></>}
    </div>}
  </div>;
}

function ProfileScreen({ notify, openMoney }) {
  const [balanceVisible, setBalanceVisible] = useState(true);
  return <div className="screen-scroll profile-screen">
    <header className="profile-head"><div className="avatar"><User weight="fill" /></div><div><small>已实名认证</small><strong>180****1860</strong></div><button type="button" onClick={() => notify("设置面板即将开放")}><GearSix /></button></header>
    <section className="profile-wallet surface-card"><div className="store-row"><span><Storefront weight="fill" />当前店铺：恭喜发财</span><ArrowRight /></div><div className="wallet-line"><div className="profile-balance"><span>账户总额 <button type="button" aria-label="切换余额可见" onClick={() => setBalanceVisible(!balanceVisible)}>{balanceVisible ? <Eye /> : <EyeSlash />}</button></span><strong>{balanceVisible ? "¥ 8,101.84" : "¥ ••••••"}</strong></div><div className="profile-money-actions"><IconButton icon={HandCoins} label="提现" tone="amber" onClick={() => openMoney("提现")} /><IconButton icon={CreditCard} label="充值" tone="blue" onClick={() => openMoney("充值")} /><IconButton icon={ListChecks} label="明细" tone="red" onClick={() => notify("资金明细已打开")} /><IconButton icon={EnvelopeSimple} label="邀请" tone="purple" onClick={() => notify("邀请码已复制")} /></div></div></section>
    <section className="order-card surface-card"><div className="section-heading"><h2>投注记录</h2><button type="button" onClick={() => notify("已打开全部记录")}>全部记录 <ArrowRight /></button></div><div className="order-stats">{[[0,"待出票"], [1,"待开奖"], [3,"已中奖"], [0,"今日中奖"]].map(([num, label]) => <button type="button" key={label} onClick={() => notify(`${label}记录已筛选`)}><strong>{num}</strong><span>{label}</span></button>)}</div></section>
    <section className="service-card surface-card">{serviceItems.map(([label, Icon]) => <button type="button" key={label} onClick={() => notify(`${label}已打开`)}><Icon weight="fill" /><span>{label}</span></button>)}</section>
    <button className="responsible-link" type="button" onClick={() => notify("责任彩票与未成年人保护")}><ShieldCheck weight="fill" /> 理性购彩 · 未成年人禁止购彩 <ArrowRight /></button>
  </div>;
}

function BottomNav({ active, setActive }) {
  return <nav className="bottom-nav" aria-label="主导航">{[["home", "大厅", House], ["matches", "比赛", CalendarDots], ["profile", "个人", User]].map(([key, label, Icon]) => <button type="button" key={key} className={active === key ? "active" : ""} onClick={() => setActive(key)}><Icon weight={active === key ? "fill" : "regular"} /><span>{label}</span></button>)}</nav>;
}

function MoneyModal({ type, close, notify }) {
  const [amount, setAmount] = useState(type === "充值" ? 100 : 200);
  return <div className="modal-backdrop" onMouseDown={close}><section className="money-modal" onMouseDown={(e) => e.stopPropagation()}><div className="modal-head"><div><span className={`modal-icon ${type === "充值" ? "blue" : "amber"}`}>{type === "充值" ? <CreditCard weight="fill" /> : <HandCoins weight="fill" />}</span><div><small>模拟资金操作</small><strong>{type}</strong></div></div><button type="button" onClick={close}><X /></button></div><label>金额（元）<div className="amount-input"><span>¥</span><input autoFocus inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} /></div></label><div className="amount-presets">{[50, 100, 200, 500].map((n) => <button type="button" key={n} className={Number(amount) === n ? "active" : ""} onClick={() => setAmount(n)}>{n}元</button>)}</div><div className="safety-note"><LockKey weight="fill" /><span>原型不会发起真实支付或资金划转</span></div><button type="button" className="primary-button" onClick={() => { close(); notify(`模拟${type}申请已提交`); }}>确认{type}</button></section></div>;
}

export function App() {
  const [active, setActive] = useState("home");
  const [toast, setToast] = useState("");
  const [moneyModal, setMoneyModal] = useState(null);
  const notify = (message) => { setToast(message); window.clearTimeout(window.__lotteryToast); window.__lotteryToast = window.setTimeout(() => setToast(""), 2200); };
  return <main className="prototype-stage"><div className="mobile-prototype"><StatusBar /><div className="app-content">{active === "home" && <HomeScreen notify={notify} onGoMatches={() => setActive("matches")} />}{active === "matches" && <MatchesScreen notify={notify} />}{active === "profile" && <ProfileScreen notify={notify} openMoney={setMoneyModal} />}</div><BottomNav active={active} setActive={setActive} />{toast && <div className="toast" role="status"><CheckCircle weight="fill" />{toast}</div>}{moneyModal && <MoneyModal type={moneyModal} close={() => setMoneyModal(null)} notify={notify} />}</div></main>;
}
