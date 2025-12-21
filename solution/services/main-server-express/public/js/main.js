// Ricerca anime/personaggi
document.getElementById('searchInput')?.addEventListener('input', function(e) {
  const searchTerm = e.target.value.toLowerCase();
  const cards = document.querySelectorAll('.anime-card');
  
  cards.forEach(card => {
    const title = card.querySelector('h3').textContent.toLowerCase();
    card.style.display = title.includes(searchTerm) ? '' : 'none';
  });
});

// Filtro per tipo
document.getElementById('typeFilter')?.addEventListener('change', function(e) {
  const selectedType = e.target.value;
  const cards = document.querySelectorAll('.anime-card');
  
  cards.forEach(card => {
    const type = card.querySelector('.type').textContent;
    card.style.display = !selectedType || type.includes(selectedType) ? '' : 'none';
  });
});

// Lazy loading immagini
if ('IntersectionObserver' in window) {
  const images = document.querySelectorAll('img[data-src]');
  const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        imageObserver.unobserve(img);
      }
    });
  });
  
  images.forEach(img => imageObserver.observe(img));
}