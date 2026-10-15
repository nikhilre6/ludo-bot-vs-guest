let game=null;
const board=document.getElementById("board"), rollBtn=document.getElementById("roll");
const route=[], map={};
for(let r=0;r<15;r++)route.push([r,7]);
for(let c=0;c<15;c++)if(c!==7)route.push([7,c]);
for(let r=14;r>=0;r--)if(r!==7)route.push([r,7]);
route.slice(0,56).forEach((p,i)=>map[i+1]=p);

function buildBoard(){
  board.innerHTML="";
  for(let r=0;r<15;r++)for(let c=0;c<15;c++){
    const cell=document.createElement("div"); cell.className="cell";
    if(r<6&&c<6)cell.classList.add("home-guest");
    if(r<6&&c>8)cell.classList.add("home-bot");
    if(r>=6&&r<=8&&c>=6&&c<=8)cell.classList.add("center");
    const pos=Object.keys(map).find(p=>map[p][0]===r&&map[p][1]===c);
    if(pos){cell.classList.add("route");cell.dataset.position=pos}
    board.appendChild(cell);
  }
}

async function api(url,opt={}){
  const res=await fetch(url,{headers:{"Content-Type":"application/json"},...opt});
  if(!res.ok)throw new Error(await res.text());
  return res.json();
}

async function newGame(){try{game=await api("/api/ludo/games",{method:"POST"});render()}catch(e){alert(e.message)}}
async function roll(){try{game=await api(`/api/ludo/games/${game.gameId}/roll`,{method:"POST"});render()}catch(e){alert(e.message)}}
async function move(i){try{game=await api(`/api/ludo/games/${game.gameId}/move`,{method:"POST",body:JSON.stringify({tokenIndex:i})});render()}catch(e){alert(e.message)}}

function posText(p){return p===0?"HOME":p===57?"FINISH":p}

function render(){
  if(!game)return;
  document.getElementById("gameId").textContent=game.gameId.substring(0,8);
  document.getElementById("turn").textContent=game.turn;
  document.getElementById("dice").textContent=game.dice||"-";
  document.getElementById("message").textContent=game.message||"";
  rollBtn.disabled=game.status!=="PLAYING"||game.turn!=="GUEST";
  renderTokenButtons(); renderTokens();
}

function renderTokenButtons(){
  const g=document.getElementById("guestTokens"),b=document.getElementById("botTokens");
  g.innerHTML="";b.innerHTML="";
  game.guestTokens.forEach((p,i)=>{
    const x=document.createElement("button");x.className="token-btn";
    x.textContent=`G${i+1}: ${posText(p)}`;
    x.disabled=game.turn!=="GUEST"||!game.dice||game.status!=="PLAYING";
    x.onclick=()=>move(i);g.appendChild(x);
  });
  game.botTokens.forEach((p,i)=>{
    const x=document.createElement("button");x.className="token-btn";
    x.textContent=`B${i+1}: ${posText(p)}`;x.disabled=true;b.appendChild(x);
  });
}

function renderTokens(){
  document.querySelectorAll(".token").forEach(x=>x.remove());
  game.guestTokens.forEach((p,i)=>addToken(p,i+1,"guest-token"));
  game.botTokens.forEach((p,i)=>addToken(p,i+1,"bot-token"));
}

function addToken(pos,n,css){
  if(!map[pos])return;
  const [r,c]=map[pos],cell=board.children[r*15+c];
  const x=document.createElement("div");x.className=`token ${css}`;x.textContent=n;
  cell.appendChild(x);
}

document.getElementById("newGame").onclick=newGame;
rollBtn.onclick=roll;
buildBoard();newGame();
