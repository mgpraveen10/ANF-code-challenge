document.addEventListener("DOMContentLoaded", (event) => {
  document
    .getElementById("submitButton")
    .setAttribute("onclick", "FetchSubmit(event)");
});

async function FetchSubmit(e) {
  e.preventDefault();
  let search = Seach_Box.text.value;
  let searchData= JSON.stringify({"textInput":search});
  console.log(searchData);
  let response = await fetch("/bin/searchbox1?textInput="+search, {
    method: "GET",
    headers: {
      "Content-type": "application/json; charset=UTF-8",
    },
  });
  let output = await response.json();
  console.log(output);
  let contetelem = document.getElementById("jsonContent");

  let contentString = "";
  if (output.length == 0) {
    alert("Your term returned 0 results");
    return;
  } else {
    output.forEach((element) => {
      contentString += `<div class="textColor text-align-center">
      <h1>${element.title}</h1>
      <h3>${element.lastModified}</h3>
      <p>${element.description}</p>
      <img src="${element.image}"/>
      </div>`;
    });
    contetelem.innerHTML = contentString;
  }
}
